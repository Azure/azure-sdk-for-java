// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
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
import com.azure.storage.common.test.shared.policy.TransientFailureInjectingHttpPipelinePolicy;
import com.azure.storage.file.datalake.models.AccessTier;
import com.azure.storage.file.datalake.models.DataLakeAudience;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileExpirationOffset;
import com.azure.storage.file.datalake.models.FileQueryArrowField;
import com.azure.storage.file.datalake.models.FileQueryArrowFieldType;
import com.azure.storage.file.datalake.models.FileQueryArrowSerialization;
import com.azure.storage.file.datalake.models.FileQueryAsyncResponse;
import com.azure.storage.file.datalake.models.FileQueryDelimitedSerialization;
import com.azure.storage.file.datalake.models.FileQueryError;
import com.azure.storage.file.datalake.models.FileQueryJsonSerialization;
import com.azure.storage.file.datalake.models.FileQueryParquetSerialization;
import com.azure.storage.file.datalake.models.FileQueryProgress;
import com.azure.storage.file.datalake.models.FileQuerySerialization;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.FileReadAsyncResponse;
import com.azure.storage.file.datalake.models.LeaseAction;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
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
import com.azure.storage.file.datalake.options.PathGetPropertiesOptions;
import com.azure.storage.file.datalake.options.ReadToFileOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import reactor.util.function.Tuple2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileAsyncApiTests extends DataLakeTestBase {
    private DataLakeFileAsyncClient fc;
    private final List<File> createdFiles = new ArrayList<>();
    private static final PathPermissions PERMISSIONS = new PathPermissions()
        .setOwner(new RolePermissions().setReadPermission(true).setWritePermission(true).setExecutePermission(true))
        .setGroup(new RolePermissions().setReadPermission(true).setExecutePermission(true))
        .setOther(new RolePermissions().setReadPermission(true));
    private static final String GROUP = null;
    private static final String OWNER = null;
    private static final List<PathAccessControlEntry> PATH_ACCESS_CONTROL_ENTRIES =
        PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");


    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemAsyncClient.createFile(generatePathName()).block();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    public void cleanup() {
        createdFiles.forEach(File::delete);
    }


    @Test
    public void createMin() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.create())
            .assertNext(r -> assertNotEquals(null, r))
            .verifyComplete();
    }

    @Test
    public void createDefaults() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.createWithResponse(
            null, null, null, null, null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.createWithResponse(
            null, null, null, null, new DataLakeRequestConditions().setIfMatch("garbage")))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createOverwrite() {
        Mono<PathInfo> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> r.create(false));

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void exists() {
        Mono<Boolean> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(DataLakePathAsyncClient::exists);

        StepVerifier.create(response)
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void doesNotExist() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.exists())
            .expectNext(false)
            .verifyComplete();
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

        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(fc.createWithResponse(null, null, headers, null, null)
            .then(fc.getPropertiesWithResponse(null)))
            .assertNext(r -> validatePathProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                null, finalContentType))
            .verifyComplete();
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

        StepVerifier.create(fc.createWithResponse(null, null, null, metadata, null)
            .then(fc.getProperties()))
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2021-04-10")
    @Test
    public void createEncryptionContext() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        Mono<PathInfo> step = dataLakeFileSystemAsyncClient.create()
            .then(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).create());

        Mono<DataLakeFileAsyncClient> getClient = Mono.just(dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName()));

        String encryptionContext = "encryptionContext";
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setEncryptionContext(encryptionContext);

        Mono<PathProperties> response1 = step
            .then(getClient)
            .flatMap(r -> r.createWithResponse(options, Context.NONE).then(r.getProperties()));

        StepVerifier.create(response1)
            .assertNext(r -> assertEquals(encryptionContext, r.getEncryptionContext()))
            .verifyComplete();

        Mono<FileReadAsyncResponse> response2 = getClient
            .flatMap(r -> r.readWithResponse(null, null, null, false));

        StepVerifier.create(response2)
            .assertNext(r -> assertEquals(encryptionContext, r.getDeserializedHeaders().getEncryptionContext()))
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths(new ListPathsOptions().setRecursive(true)))
            .expectNextCount(1)
            .assertNext(r -> assertEquals(encryptionContext, r.getEncryptionContext()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID) {

        Mono<Response<PathInfo>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.createWithResponse(null, null, null, null, drc);
            });

        assertAsyncResponseStatusCode(response, 201);
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
        Mono<Response<PathInfo>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.createWithResponse(null, null, null, null, drc);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
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
        assertAsyncResponseStatusCode(fc.createWithResponse(
            "0777", "0057", null, null, null), 201);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);

        StepVerifier.create(fc.createWithResponse(options, null).then(fc.getAccessControl()))
            .assertNext(r -> {
                assertEquals(pathAccessControlEntries.get(0), r.getAccessControlList().get(0)); // testing if owner is set the same
                assertEquals(pathAccessControlEntries.get(1), r.getAccessControlList().get(1)); // testing if owner is set the same
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName);

        StepVerifier.create(fc.createWithResponse(options, null).then(fc.getAccessControl()))
            .assertNext(r -> {
                assertEquals(ownerName, r.getOwner());
                assertEquals(groupName, r.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createOptionsWithNullOwnerAndGroup() {
        StepVerifier.create(fc.createWithResponse(null, null).then(fc.getAccessControl()))
            .assertNext(r -> {
                assertEquals("$superuser", r.getOwner());
                assertEquals("$superuser", r.getGroup());
            })
            .verifyComplete();
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

        assertAsyncResponseStatusCode(fc.createWithResponse(options, null), 201);
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

        assertAsyncResponseStatusCode(fc.createWithResponse(options, null), 201);

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> {
                for (String k : metadata.keySet()) {
                    assertTrue(r.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), r.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");

        StepVerifier.create(fc.createWithResponse(options, null).then(fc.getAccessControlWithResponse(
            true, null, null)))
            .assertNext(r -> assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(),
                r.getValue().getPermissions().toString()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createOptionsWithLeaseId() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId)
            .setLeaseDuration(15);

        assertAsyncResponseStatusCode(fc.createWithResponse(options, null), 201);
    }

    @Test
    public void createOptionsWithLeaseIdError() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId);

        // lease duration must also be set, or else exception is thrown
        StepVerifier.create(fc.createWithResponse(options, null))
            .verifyError(DataLakeStorageException.class);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createOptionsWithLeaseDuration() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId);

        assertAsyncResponseStatusCode(fc.createWithResponse(options, null), 201);

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> {
                assertEquals(LeaseStatusType.LOCKED, r.getLeaseStatus());
                assertEquals(LeaseStateType.LEASED, r.getLeaseState());
                assertEquals(LeaseDurationType.FIXED, r.getLeaseDuration());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("timeExpiresOnOptionsSupplier")
    public void createOptionsWithTimeExpiresOn(DataLakePathScheduleDeletionOptions deletionOptions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions);

        assertAsyncResponseStatusCode(fc.createWithResponse(options, null), 201);
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

        assertAsyncResponseStatusCode(fc.createWithResponse(options, null), 201);

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> compareDatesWithPrecision(r.getExpiresOn(), r.getCreationTime().plusDays(6)))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsMin() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.createIfNotExists().then(fc.exists()))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDefaults() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions(), null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsOverwrite() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        assertAsyncResponseStatusCode(fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions(), null),
            201);

        StepVerifier.create(fc.exists())
            .expectNext(true)
            .verifyComplete();

        assertAsyncResponseStatusCode(fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions(), null),
            409);
    }

    @Test
    public void createIfNotExistsExists() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.createIfNotExists().then(fc.exists()))
            .assertNext(Assertions::assertTrue)
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null", "control, disposition, encoding, language, type"})
    public void createIfNotExistsHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, String contentType) {
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);

        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions().setPathHttpHeaders(headers),
            null)
            .then(fc.getPropertiesWithResponse(null)))
            .assertNext(r -> validatePathProperties(r, cacheControl, contentDisposition, contentEncoding,
            contentLanguage, null, finalContentType))
            .verifyComplete();
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

        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions().setMetadata(metadata),
            Context.NONE)
            .then(fc.getProperties()))
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsPermissionsAndUmask() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        assertAsyncResponseStatusCode(fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions()
            .setPermissions("0777").setUmask("0057"), Context.NONE), 201);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2021-04-10")
    @Test
    public void createIfNotExistsEncryptionContext() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        Mono<PathInfo> step = dataLakeFileSystemAsyncClient.create()
            .then(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).create());

        Mono<DataLakeFileAsyncClient> getClient = Mono.just(dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName()));

        String encryptionContext = "encryptionContext";
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setEncryptionContext(encryptionContext);

        Mono<PathProperties> response1 = step
            .then(getClient)
            .flatMap(r -> r.createIfNotExistsWithResponse(options, Context.NONE).then(r.getProperties()));

        StepVerifier.create(response1)
            .assertNext(r -> assertEquals(encryptionContext, r.getEncryptionContext()))
            .verifyComplete();

        Mono<FileReadAsyncResponse> response2 = getClient
            .flatMap(r -> r.readWithResponse(null, null, null, false));

        StepVerifier.create(response2)
            .assertNext(r -> assertEquals(encryptionContext, r.getDeserializedHeaders().getEncryptionContext()))
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths(new ListPathsOptions().setRecursive(true)))
            .expectNextCount(1)
            .assertNext(r -> assertEquals(encryptionContext, r.getEncryptionContext()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithACL() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);

        StepVerifier.create(fc.createIfNotExistsWithResponse(options, null).then(fc.getAccessControl()))
            .assertNext(r -> {
                assertEquals(pathAccessControlEntries.get(0), r.getAccessControlList().get(0));
                assertEquals(pathAccessControlEntries.get(1), r.getAccessControlList().get(1));
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithOwnerAndGroup() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName);

        StepVerifier.create(fc.createIfNotExistsWithResponse(options, null).then(fc.getAccessControl()))
            .assertNext(r -> {
                assertEquals(ownerName, r.getOwner());
                assertEquals(groupName, r.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsOptionsWithNullOwnerAndGroup() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(null).setGroup(null);

        StepVerifier.create(fc.createIfNotExistsWithResponse(options, null).then(fc.getAccessControl()))
            .assertNext(r -> {
                assertEquals("$superuser", r.getOwner());
                assertEquals("$superuser", r.getGroup());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null,application/octet-stream", "control,disposition,encoding,language,null,type"},
        nullValues = "null")
    public void createIfNotExistsOptionsWithPathHttpHeaders(String cacheControl, String contentDisposition,
        String contentEncoding, String contentLanguage, byte[] contentMD5, String contentType) {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders);

        assertAsyncResponseStatusCode(fc.createIfNotExistsWithResponse(options, null), 201);
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createIfNotExistsOptionsWithMetadata(String key1, String value1, String key2, String value2) {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setMetadata(metadata);

        assertAsyncResponseStatusCode(fc.createIfNotExistsWithResponse(options, null), 201);

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> {
                for (String k : metadata.keySet()) {
                    assertTrue(r.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), r.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsOptionsWithPermissionsAndUmask() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");

        StepVerifier.create(fc.createIfNotExistsWithResponse(options, null).then(fc.getAccessControlWithResponse(
            true, null, null)))
            .assertNext(r -> assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(),
            r.getValue().getPermissions().toString()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithLeaseId() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        String leaseId = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId)
            .setLeaseDuration(15);

        assertAsyncResponseStatusCode(fc.createIfNotExistsWithResponse(options, null), 201);
    }

    @Test
    public void createIfNotExistsOptionsWithLeaseIdError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId);

        StepVerifier.create(fc.createIfNotExistsWithResponse(options, null))
            .verifyError(DataLakeStorageException.class);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithLeaseDuration() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId);

        assertAsyncResponseStatusCode(fc.createIfNotExistsWithResponse(options, null), 201);

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> {
                assertEquals(LeaseStatusType.LOCKED, r.getLeaseStatus());
                assertEquals(LeaseStateType.LEASED, r.getLeaseState());
                assertEquals(LeaseDurationType.FIXED, r.getLeaseDuration());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("timeExpiresOnOptionsSupplier")
    public void createIfNotExistsOptionsWithTimeExpiresOn(DataLakePathScheduleDeletionOptions deletionOptions) {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions);

        assertAsyncResponseStatusCode(fc.createIfNotExistsWithResponse(options, null), 201);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithTimeToExpireRelativeToNow() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        DataLakePathScheduleDeletionOptions deletionOptions = new DataLakePathScheduleDeletionOptions(Duration.ofDays(6));
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions);

        assertAsyncResponseStatusCode(fc.createIfNotExistsWithResponse(options, null), 201);

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> compareDatesWithPrecision(r.getExpiresOn(), r.getCreationTime().plusDays(6)))
            .verifyComplete();
    }
    @Test
    public void deleteMin() {
        assertAsyncResponseStatusCode(fc.deleteWithResponse(
            null, null, null), 200);
    }

    @Test
    public void deleteFileDoesNotExistAnymore() {
        StepVerifier.create(fc.deleteWithResponse(null, null, null)
            .then(fc.getPropertiesWithResponse(null)))
            .verifyErrorSatisfies(r -> DataLakeTestBase.assertExceptionStatusCodeAndMessage(r, 404,
            BlobErrorCode.BLOB_NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                       String leaseID) {
        Mono<Response<Void>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.deleteWithResponse(drc);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        Mono<Response<Void>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.deleteWithResponse(drc);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExists() {
        StepVerifier.create(fc.deleteIfExists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsMin() {
        assertAsyncResponseStatusCode(fc.deleteIfExistsWithResponse(null, null), 200);
    }

    @Test
    public void deleteIfExistsFileDoesNotExistAnymore() {
        assertAsyncResponseStatusCode(fc.deleteIfExistsWithResponse(null, null), 200);
        StepVerifier.create(fc.getPropertiesWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExist() {
        assertAsyncResponseStatusCode(fc.deleteIfExistsWithResponse(null, null), 200);
        assertAsyncResponseStatusCode(fc.deleteIfExistsWithResponse(null, null), 404);

    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        Mono<Response<Boolean>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(false)
                    .setRequestConditions(drc);
                return fc.deleteIfExistsWithResponse(options, null);
            });

        assertAsyncResponseStatusCode(response, 200);

    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        Mono<Response<Boolean>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc);
                return fc.deleteIfExistsWithResponse(options, null);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setPermissionsMin() {
        StepVerifier.create(fc.setPermissions(PERMISSIONS, GROUP, OWNER))
            .assertNext(r -> {
                assertNotNull(r.getETag());
                assertNotNull(r.getLastModified());
            })
            .verifyComplete();
    }

    @Test
    public void setPermissionsWithResponse() {
        assertAsyncResponseStatusCode(fc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, null),
            200);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setPermissionsAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        Mono<Response<PathInfo>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, drc);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setPermissionsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        Mono<Response<PathInfo>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, drc);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setPermissionsError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setACLMin() {
        StepVerifier.create(fc.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER))
            .assertNext(r -> {
                assertNotNull(r.getETag());
                assertNotNull(r.getLastModified());
            })
            .verifyComplete();
    }

    @Test
    public void setACLWithResponse() {
        assertAsyncResponseStatusCode(fc.setAccessControlListWithResponse(
            PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, null), 200);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setAclAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID) {
        Mono<Response<PathInfo>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, drc);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setAclACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        Mono<Response<PathInfo>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, drc);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setACLError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER))
            .verifyError(DataLakeStorageException.class);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursive() {
        StepVerifier.create(fc.setAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES))
            .assertNext(r -> {
                assertEquals(0L, r.getCounters().getChangedDirectoriesCount());
                assertEquals(1L, r.getCounters().getChangedFilesCount());
                assertEquals(0L, r.getCounters().getFailedChangesCount());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursive() {
        StepVerifier.create(fc.updateAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES))
            .assertNext(r -> {
                assertEquals(0L, r.getCounters().getChangedDirectoriesCount());
                assertEquals(1L, r.getCounters().getChangedFilesCount());
                assertEquals(0L, r.getCounters().getFailedChangesCount());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursive() {
        List<PathRemoveAccessControlEntry> removeAccessControlEntries = PathRemoveAccessControlEntry.parseList(
            "mask,default:user,default:group,user:ec3595d6-2c17-4696-8caa-7e139758d24a,"
                + "group:ec3595d6-2c17-4696-8caa-7e139758d24a,default:user:ec3595d6-2c17-4696-8caa-7e139758d24a,"
                + "default:group:ec3595d6-2c17-4696-8caa-7e139758d24a");

        StepVerifier.create(fc.removeAccessControlRecursive(removeAccessControlEntries))
            .assertNext(r -> {
                assertEquals(0L, r.getCounters().getChangedDirectoriesCount());
                assertEquals(1L, r.getCounters().getChangedFilesCount());
                assertEquals(0L, r.getCounters().getFailedChangesCount());
            })
            .verifyComplete();
    }

    @Test
    public void getAccessControlMin() {
        StepVerifier.create(fc.getAccessControl())
            .assertNext(r -> {
                assertNotNull(r.getAccessControlList());
                assertNotNull(r.getPermissions());
                assertNotNull(r.getOwner());
                assertNotNull(r.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void getAccessControlWithResponse() {
        assertAsyncResponseStatusCode(fc.getAccessControlWithResponse(
            false, null, null), 200);
    }

    @Test
    public void getAccessControlReturnUpn() {
        assertAsyncResponseStatusCode(fc.getAccessControlWithResponse(
            true, null, null), 200);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void getAccessControlAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String leaseID) {
        Mono<Response<PathAccessControl>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.getAccessControlWithResponse(false, drc, null);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void getAccessControlACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                       String noneMatch, String leaseID) {
        if (GARBAGE_LEASE_ID.equals(leaseID)) {
            return; // known bug in DFS endpoint
        }

        Mono<Response<PathAccessControl>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.getAccessControlWithResponse(false, drc, null);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getPropertiesDefault() {
        StepVerifier.create(fc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();
                PathProperties properties = r.getValue();

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
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesMin() {
        assertAsyncResponseStatusCode(fc.getPropertiesWithResponse(null), 200);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void getPropertiesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                String leaseID) {
        Mono<Response<PathProperties>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.getPropertiesWithResponse(drc);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void getPropertiesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                    String leaseID) {
        Mono<Response<PathProperties>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.getPropertiesWithResponse(drc);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getPropertiesError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.getProperties())
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException ex = assertInstanceOf(DataLakeStorageException.class, r);
                assertTrue(ex.getMessage().contains("BlobNotFound"));
            });
    }

    @Test
    public void setHTTPHeadersNull() {
        StepVerifier.create(fc.setHttpHeadersWithResponse(null, null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void setHTTPHeadersMin() throws NoSuchAlgorithmException {
        Mono<PathProperties> response = fc.getProperties()
            .flatMap(r -> {
                PathHttpHeaders headers;
                try {
                    headers = new PathHttpHeaders()
                        .setContentEncoding(r.getContentEncoding())
                        .setContentDisposition(r.getContentDisposition())
                        .setContentType("type")
                        .setCacheControl(r.getCacheControl())
                        .setContentLanguage(r.getContentLanguage())
                        .setContentMd5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5")
                            .digest(DATA.getDefaultBytes())));
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                return fc.setHttpHeaders(headers).then(fc.getProperties());
            });

        StepVerifier.create(response)
            .assertNext(r -> assertEquals("type", r.getContentType()))
            .verifyComplete();
    }
    @ParameterizedTest
    @MethodSource("setHTTPHeadersHeadersSupplier")
    public void setHTTPHeadersHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                      String contentLanguage, byte[] contentMD5, String contentType) {
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        Mono<Response<PathProperties>> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fc.setHttpHeaders(putHeaders))
            .then(fc.getPropertiesWithResponse(null));

        StepVerifier.create(response)
            .assertNext(r -> validatePathProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
            contentMD5, contentType))
                .verifyComplete();
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
        Mono<Response<Void>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.setHttpHeadersWithResponse(null, drc);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setHttpHeadersACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        Mono<Response<Void>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.setHttpHeadersWithResponse(null, drc);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setHTTPHeadersError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.setHttpHeaders(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");

        StepVerifier.create(fc.setMetadata(metadata).then(fc.getProperties()))
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
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

        assertAsyncResponseStatusCode(fc.setMetadataWithResponse(metadata, null), statusCode);

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }
    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setMetadataAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                              String leaseID) {
        Mono<Response<Void>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.setMetadataWithResponse(null, drc);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setMetadataACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                  String leaseID) {
        Mono<Response<Void>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.setMetadataWithResponse(null, drc);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }
    @Test
    public void setMetadataError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.setMetadata(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void readAllNull() {
        Mono<byte[]> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fc.readWithResponse(null, null, null, false))
                .flatMap(r -> {
                    HttpHeaders headers = r.getHeaders();

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
                    assertNotNull(r.getDeserializedHeaders().getCreationTime());
                    return FluxUtil.collectBytesInByteBufferStream(r.getValue());
                });


        StepVerifier.create(response)
            .assertNext(bytes -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), bytes))
            .verifyComplete();
    }

    @Test
    public void readEmptyFile() {
        Flux<ByteBuffer> response = dataLakeFileSystemAsyncClient.createFile("emptyFile")
            .flatMapMany(DataLakeFileAsyncClient::read);

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(0, r.array().length))
            .verifyComplete();

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
        DataLakeFileAsyncClient fileAsyncClient = getFileAsyncClient(getDataLakeCredential(), fc.getPathUrl(),
            new MockRetryRangeResponsePolicy("bytes=2-6"));

        // Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        // NOT thrown because the types would not match.

        Mono<byte[]> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fileAsyncClient.readWithResponse(new FileRange(2, 5L),
                new DownloadRetryOptions().setMaxRetryRequests(3), null, false))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()));

        StepVerifier.create(response)
            .verifyError(IOException.class);
    }

    @Test
    public void readMin() {
        Mono<byte[]> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(FluxUtil.collectBytesInByteBufferStream(fc.read()));

        StepVerifier.create(response)
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("readRangeSupplier")
    public void readRange(long offset, Long count, String expectedData) {
        FileRange range = (count == null) ? new FileRange(offset) : new FileRange(offset, count);

        Mono<byte[]> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fc.readWithResponse(range, null, null, false))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()));

        StepVerifier.create(response)
            .assertNext(bytes -> assertArrayEquals(expectedData.getBytes(), bytes))
            .verifyComplete();
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
        Mono<FileReadAsyncResponse> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.readWithResponse(null, null, drc, false);
            });

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void readACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                           String leaseID) {
        Mono<FileReadAsyncResponse> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.readWithResponse(null, null, drc, false);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void readMd5() {
        Mono<FileReadAsyncResponse> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fc.readWithResponse(new FileRange(0, 3L), null, null,
                true));

        StepVerifier.create(response)
            .assertNext(r -> {
                byte[] contentMD5 = r.getHeaders().getValue(HttpHeaderName.CONTENT_MD5).getBytes();
                try {
                    TestUtils.assertArraysEqual(
                        Base64.getEncoder().encode(
                            MessageDigest.getInstance("MD5").digest(DATA.getDefaultText().substring(0, 3).getBytes())),
                        contentMD5);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    @Test
    public void readRetryDefault() {
        Mono<byte[]> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
                .flatMap(r -> {
                    DataLakeFileAsyncClient failureFileAsyncClient = getFileAsyncClient(getDataLakeCredential(),
                        fc.getFileUrl(), new MockFailureResponsePolicy(5));
                    return FluxUtil.collectBytesInByteBufferStream(failureFileAsyncClient.read());
                });

        StepVerifier.create(response)
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @Test
    public void downloadFileExists() throws IOException {
        File testFile = new File(prefix + ".txt");
        testFile.deleteOnExit();
        createdFiles.add(testFile);

        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        Mono<PathProperties> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fc.readToFile(testFile.getPath()));

        // Default overwrite is false so this should fail
        StepVerifier.create(response)
            .verifyErrorSatisfies(r -> {
                UncheckedIOException ex = assertInstanceOf(UncheckedIOException.class, r);
                assertInstanceOf(FileAlreadyExistsException.class, ex.getCause());
            });
    }

    @Test
    public void downloadFileExistsSucceeds() throws IOException {
        File testFile = new File(prefix + ".txt");
        testFile.deleteOnExit();
        createdFiles.add(testFile);

        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        Mono<PathProperties> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fc.readToFile(testFile.getPath(), true));

        StepVerifier.create(response)
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));

    }

    @Test
    public void downloadFileDoesNotExist() throws IOException {
        File testFile = new File(prefix + ".txt");
        testFile.deleteOnExit();
        createdFiles.add(testFile);

        if (testFile.exists()) {
            assertTrue(testFile.delete());
        }

        Mono<PathProperties> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fc.readToFile(testFile.getPath(), true));

        StepVerifier.create(response)
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

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

        Set<OpenOption> openOptions = new HashSet<>(Arrays.asList(
            StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE));

        Mono<Response<PathProperties>> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fc.readToFileWithResponse(testFile.getPath(), null, null,
                null, null, false, openOptions));

        StepVerifier.create(response)
            .assertNext(r -> {
                try {
                    assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()),
                        StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    @Test
    public void downloadFileExistOpenOptions() throws IOException {
        File testFile = new File(prefix + ".txt");
        testFile.deleteOnExit();
        createdFiles.add(testFile);

        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        Set<OpenOption> openOptions = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.READ, StandardOpenOption.WRITE));

        Mono<Response<PathProperties>> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(fc.readToFileWithResponse(testFile.getPath(), null, null,
                null, null, false, openOptions));

        StepVerifier.create(response)
            .assertNext(r -> {
                try {
                    assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()),
                        StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("downloadFileSupplier")
    public void downloadFile(int fileSize) {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(), true)
            .then(fc.readToFileWithResponse(outFile.toPath().toString(), null,
                new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024),
            null, null, false, null)))
            .assertNext(r -> assertEquals(fileSize, r.getValue().getFileSize()))
            .verifyComplete();

        compareFiles(file, outFile, 0, fileSize);

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
    public void downloadFileAsyncBufferCopy(int fileSize) {
        String fileSystemName = generateFileSystemName();
        DataLakeServiceAsyncClient datalakeServiceAsyncClient = new DataLakeServiceClientBuilder()
            .endpoint(ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint())
            .credential(getDataLakeCredential())
            .buildAsyncClient();

        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        Mono<Response<PathProperties>> response = datalakeServiceAsyncClient.createFileSystem(fileSystemName)
                .flatMap(r -> {
                    DataLakeFileAsyncClient fileAsyncClient = r.getFileAsyncClient(generatePathName());
                    return fileAsyncClient.uploadFromFile(file.toPath().toString(), true)
                        .then(fileAsyncClient.readToFileWithResponse(outFile.toPath().toString(), null,
                            new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024),
                        null, null, false, null));
                });

        StepVerifier.create(response.map(Response::getValue))
            .assertNext(properties -> assertEquals(fileSize, properties.getFileSize()))
            .verifyComplete();

        compareFiles(file, outFile, 0, fileSize);
    }

    @ParameterizedTest
    @MethodSource("downloadFileRangeSupplier")
    public void downloadFileRange(FileRange range) {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = new File(testResourceNamer.randomName("", 60));
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(), true)
            .then(fc.readToFileWithResponse(outFile.toPath().toString(), range, null,
            null, null, false, null)))
            .assertNext(r -> compareFiles(file, outFile, range.getOffset(), range.getCount()))
            .verifyComplete();
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

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(), true)
            .then(fc.readToFileWithResponse(outFile.toPath().toString(),
                new FileRange(DATA.getDefaultDataSizeLong() + 1), null, null,
                null, false, null)))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void downloadFileCountNull() {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(), true)
            .then(fc.readToFileWithResponse(outFile.toPath().toString(), new FileRange(0),
            null, null, null, false, null)))
            .assertNext(r ->  compareFiles(file, outFile, 0, DATA.getDefaultDataSizeLong()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void downloadFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID) {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = new File(testResourceNamer.randomName("", 60));
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        Mono<Response<PathProperties>> response = fc.uploadFromFile(file.toPath().toString(), true)
            .then(Mono.zip(setupPathMatchConditionAsync(fc, match), setupPathLeaseConditionAsync(fc, leaseID)))
            .flatMap(tuple -> {
                String newLease = tuple.getT2();
                String newMatch = tuple.getT1();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions bro = new DataLakeRequestConditions()
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setLeaseId(newLease);
                return fc.readToFileWithResponse(outFile.toPath().toString(), null,
                    null, null, bro, false, null);
            });

        StepVerifier.create(response)
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void downloadFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String leaseID) {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        Mono<Response<PathProperties>> response = fc.uploadFromFile(file.toPath().toString(), true)
                .then(Mono.zip(setupPathMatchConditionAsync(fc, noneMatch), setupPathLeaseConditionAsync(fc, leaseID)))
                .flatMap(tuple -> {
                    String newNoneMatch = tuple.getT1();
                    if ("null".equals(newNoneMatch)) {
                        newNoneMatch = null;
                    }
                    DataLakeRequestConditions bro = new DataLakeRequestConditions()
                            .setIfModifiedSince(modified)
                            .setIfUnmodifiedSince(unmodified)
                            .setIfMatch(match)
                            .setIfNoneMatch(newNoneMatch)
                            .setLeaseId(leaseID);
                    return fc.readToFileWithResponse(outFile.toPath().toString(), null,
                            null, null, bro, false, null);
                });

        StepVerifier.create(response)
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertTrue(Objects.equals(e.getErrorCode(), "ConditionNotMet") || Objects.equals(e.getErrorCode(),
                        "LeaseIdMismatchWithBlobOperation"));
            });

    }

    @LiveOnly
    @Test
    public void downloadFileEtagLock() throws IOException {
        File file = getRandomFile(Constants.MB);
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = new File(prefix);
        Files.deleteIfExists(outFile.toPath());
        outFile.deleteOnExit();
        createdFiles.add(outFile);

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

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(), true)
            .then(facDownloading.readToFileWithResponse(outFile.toPath().toString(), null, options,
            null, null, false, null)))
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
        sleepIfRunningAgainstService(500);
        assertFalse(outFile.exists());
    }

    @SuppressWarnings("deprecation")
    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {100, 8 * 1026 * 1024 + 10})
    public void downloadFileProgressReceiver(int fileSize) {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        MockReceiver mockReceiver = new MockReceiver();

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(), true)
            .then(fc.readToFileWithResponse(outFile.toPath().toString(), null,
                new ParallelTransferOptions().setProgressReceiver(mockReceiver),
                new DownloadRetryOptions().setMaxRetryRequests(3), null, false,
                    null)))
            .assertNext(r -> {
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
            })
            .verifyComplete();
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

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        MockProgressListener mockListener = new MockProgressListener();

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(), true)
            .then(fc.readToFileWithResponse(outFile.toPath().toString(), null,
                new ParallelTransferOptions().setProgressListener(mockListener),
                new DownloadRetryOptions().setMaxRetryRequests(3), null, false,
                    null)))
            .assertNext(r -> {
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
            })
            .verifyComplete();
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
        assertAsyncResponseStatusCode(fc.renameWithResponse(null, generatePathName(),
            null, null, null), 201);
    }

    @Test
    public void renameWithResponse() {
        StepVerifier.create(fc.renameWithResponse(null, generatePathName(),
            null, null, null)
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(piece -> assertEquals(200, piece.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .verifyErrorSatisfies(r -> assertInstanceOf(DataLakeStorageException.class, r));
    }

    @Test
    public void renameFilesystemWithResponse() {
        Mono<Response<PathProperties>> response = primaryDataLakeServiceAsyncClient.createFileSystem(generateFileSystemName())
            .flatMap(r -> fc.renameWithResponse(r.getFileSystemName(), generatePathName(),
                null, null, null))
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null));

        StepVerifier.create(response)
            .assertNext(p -> assertEquals(p.getStatusCode(), 200))
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .verifyErrorSatisfies(r -> assertInstanceOf(DataLakeStorageException.class, r));
    }

    @Test
    public void renameError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.renameWithResponse(null, generatePathName(), null,
            null, null))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @CsvSource({",", "%20%25,%20%25", "%20%25,", ",%20%25"})
    public void renameUrlEncoded(String source, String destination) {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName() + source);

        Mono<Response<PathProperties>> response = fc.create()
            .then(fc.renameWithResponse(null, generatePathName() + destination,
                null, null, null))
            .flatMap(r -> {
                assertEquals(201, r.getStatusCode());
                return r.getValue().getPropertiesWithResponse(null);
            });

        StepVerifier.create(response)
            .assertNext(piece -> assertEquals(200, piece.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void renameSourceAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID) {
        Mono<Response<DataLakePathAsyncClient>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.renameWithResponse(null, generatePathName(), drc, null, null);
            });

        assertAsyncResponseStatusCode(response, 201);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void renameSourceACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String leaseID) {
        Mono<Response<DataLakePathAsyncClient>> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                    .setLeaseId(leaseID)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified);
                return fc.renameWithResponse(null, generatePathName(), drc, null, null);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }
    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void renameDestAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        String pathName = generatePathName();

        Mono<Response<DataLakePathAsyncClient>> response = dataLakeFileSystemAsyncClient.createFile(pathName)
            .flatMap(r -> Mono.zip(setupPathLeaseConditionAsync(r, leaseID), setupPathMatchConditionAsync(r, match)))
            .flatMap(r -> {
                String newLease = r.getT1();
                String newMatch = r.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified);
                return fc.renameWithResponse(null, pathName, null, drc, null);
            });

        assertAsyncResponseStatusCode(response, 201);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void renameDestACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        String pathName = generatePathName();

        Mono<Response<DataLakePathAsyncClient>> response = dataLakeFileSystemAsyncClient.createFile(pathName)
            .flatMap(r -> Mono.zip(setupPathLeaseConditionAsync(r, leaseID), setupPathMatchConditionAsync(r, noneMatch)))
            .flatMap(r -> {
                String newNoneMatch = r.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                    .setLeaseId(leaseID)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified);
                return fc.renameWithResponse(null, pathName, null, drc, null);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
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

        String sas = dataLakeFileSystemAsyncClient
            .generateSas(new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions));
        DataLakeFileAsyncClient client = getFileAsyncClient(sas, dataLakeFileSystemAsyncClient.getFileSystemUrl(),
            fc.getFilePath());

        Mono<Response<PathProperties>> response = client.rename(dataLakeFileSystemAsyncClient.getFileSystemName(),
            generatePathName())
            .flatMap(r -> r.getPropertiesWithResponse(null));

        assertAsyncResponseStatusCode(response, 200);
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

        String sas = "?" + dataLakeFileSystemAsyncClient
            .generateSas(new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions));
        DataLakeFileAsyncClient client = getFileAsyncClient(sas, dataLakeFileSystemAsyncClient.getFileSystemUrl(),
            fc.getFilePath());

        Mono<Response<PathProperties>> response = client.rename(dataLakeFileSystemAsyncClient.getFileSystemName(),
            generatePathName())
            .flatMap(r -> r.getPropertiesWithResponse(null));

        assertAsyncResponseStatusCode(response, 200);
    }

    @Test
    public void appendDataMin() {
        StepVerifier.create(fc.append(DATA.getDefaultBinaryData(), 0))
            .verifyComplete();
    }

    @Test
    public void appendData() {
        StepVerifier.create(fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null, null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();
                assertEquals(202, r.getStatusCode());
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertNotNull(headers.getValue(HttpHeaderName.DATE));
                assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();
    }

    @Test
    public void appendDataMd5() throws NoSuchAlgorithmException {
        byte[] md5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultText().getBytes());

        Mono<Response<Void>> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> r.appendWithResponse(DATA.getDefaultBinaryData(), 0, md5, null));

        StepVerifier.create(response)
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();
                assertEquals(202, r.getStatusCode());
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertNotNull(headers.getValue(HttpHeaderName.DATE));
                assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("appendDataIllegalArgumentsSupplier")
    public void appendDataIllegalArguments(Flux<ByteBuffer> is, long dataSize, Class<? extends Throwable> exceptionType) {
        StepVerifier.create(fc.append(is, 0, dataSize))
            .verifyError(exceptionType);
    }

    private static Stream<Arguments> appendDataIllegalArgumentsSupplier() {
        return Stream.of(
            // is | dataSize || exceptionType
            Arguments.of(null, DATA.getDefaultDataSizeLong(), NullPointerException.class),
            Arguments.of(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong() + 1, UnexpectedLengthException.class),
            Arguments.of(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong() - 1, UnexpectedLengthException.class)
        );
    }

    @Test
    public void appendDataEmptyBody() {
        Mono<Void> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> r.append(BinaryData.fromBytes(new byte[0]), 0));

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void appendDataNullBody() {
        Mono<Void> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> r.append(null, 0, 0));

        StepVerifier.create(response)
            .verifyError(NullPointerException.class);
    }

    @Test
    public void appendDataLease() {
        Mono<Response<Void>> response = setupPathLeaseConditionAsync(fc, RECEIVED_LEASE_ID)
            .flatMap(r -> fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null, r));

        assertAsyncResponseStatusCode(response, 202);
    }

    @Test
    public void appendDataLeaseFail() {
        StepVerifier.create(setupPathLeaseConditionAsync(fc, RECEIVED_LEASE_ID)
            .then(fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null, GARBAGE_LEASE_ID)))
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(412, e.getResponse().getStatusCode());
            });
    }


    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void appendDataLeaseAcquire() {
        Mono<DataLakeFileAsyncClient> client = dataLakeFileSystemAsyncClient.createFileIfNotExists(generatePathName());

        Mono<Response<Void>> response1 = client.flatMap(r -> {
            DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
                .setLeaseAction(LeaseAction.ACQUIRE)
                .setProposedLeaseId(CoreUtils.randomUuid().toString())
                .setLeaseDuration(15);
            return r.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions);
        });

        assertAsyncResponseStatusCode(response1, 202);

        Mono<PathProperties> response2 = client.flatMap(DataLakePathAsyncClient::getProperties);

        StepVerifier.create(response2)
            .assertNext(r -> {
                assertEquals(LeaseStatusType.LOCKED, r.getLeaseStatus());
                assertEquals(LeaseStateType.LEASED, r.getLeaseState());
                assertEquals(LeaseDurationType.FIXED, r.getLeaseDuration());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void appendDataLeaseAutoRenew() {
        Mono<Tuple2<Response<Void>, PathProperties>> response = dataLakeFileSystemAsyncClient
            .createFileIfNotExists(generatePathName())
            .flatMap(file -> {
                String leaseId = CoreUtils.randomUuid().toString();
                DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(file, leaseId);

                return Mono.zip(leaseClient.acquireLease(15), Mono.just(file), Mono.just(leaseId));
            })
            .flatMap(r -> {
                DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
                    .setLeaseAction(LeaseAction.AUTO_RENEW)
                    .setLeaseId(r.getT3());
                return Mono.zip(r.getT2().appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions),
                    Mono.just(r.getT2()));
            }).flatMap(tuple -> Mono.zip(Mono.just(tuple.getT1()), tuple.getT2().getProperties()));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(202, r.getT1().getStatusCode());
                assertEquals(LeaseStatusType.LOCKED, r.getT2().getLeaseStatus());
                assertEquals(LeaseStateType.LEASED, r.getT2().getLeaseState());
                assertEquals(LeaseDurationType.FIXED, r.getT2().getLeaseDuration());
            });
    }

    @Test
    public void appendDataLeaseRelease() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(LeaseAction.RELEASE)
            .setLeaseId(leaseId)
            .setFlush(true);

        Mono<Tuple2<Response<Void>, PathProperties>> response = dataLakeFileSystemAsyncClient
            .createFileIfNotExists(generatePathName())
            .flatMap(r -> {
                DataLakeLeaseAsyncClient leaseClient = createLeaseAsyncClient(r, leaseId);

                Mono<Response<Void>> response1 = leaseClient.acquireLease(15)
                    .then(r.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions));

                return Mono.zip(response1, Mono.just(r));
            })
            .flatMap(tuple -> {
                Mono<PathProperties> response2 = tuple.getT2().getProperties();
                return Mono.zip(Mono.just(tuple.getT1()), response2);
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(202, r.getT1().getStatusCode());
                assertEquals(LeaseStatusType.UNLOCKED, r.getT2().getLeaseStatus());
                assertEquals(LeaseStateType.AVAILABLE, r.getT2().getLeaseState());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void appendDataLeaseAcquireRelease() {
        Mono<DataLakeFileAsyncClient> client = dataLakeFileSystemAsyncClient.createFileIfNotExists(generatePathName());

        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(LeaseAction.ACQUIRE_RELEASE)
            .setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setLeaseDuration(15)
            .setFlush(true);

        Mono<Response<Void>> response1 = client.flatMap(r -> r.appendWithResponse(DATA.getDefaultBinaryData(),
            0, appendOptions));

        assertAsyncResponseStatusCode(response1, 202);

        Mono<PathProperties> response2 = client.flatMap(DataLakePathAsyncClient::getProperties);

        StepVerifier.create(response2)
            .assertNext(r -> {
                assertEquals(LeaseStatusType.UNLOCKED, r.getLeaseStatus());
                assertEquals(LeaseStateType.AVAILABLE, r.getLeaseState());
            })
            .verifyComplete();
    }

    @Test
    public void appendDataError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null))
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(404, e.getResponse().getStatusCode());
            });
    }

    @Test
    public void appendDataRetryOnTransientFailure() {
        DataLakeFileAsyncClient clientWithFailure = getFileAsyncClient(getDataLakeCredential(), fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());

        Mono<byte[]> response = clientWithFailure.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true))
            .then(FluxUtil.collectBytesInByteBufferStream(fc.read()));

        StepVerifier.create(response)
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void appendDataFlush() {
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions().setFlush(true);

        StepVerifier.create(fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();

                assertEquals(202, r.getStatusCode());
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertNotNull(headers.getValue(HttpHeaderName.DATE));
                assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fc.read()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }
    @Test
    public void appendBinaryDataMin() {
        StepVerifier.create(fc.append(DATA.getDefaultBinaryData(), 0))
            .verifyComplete();
    }

    @Test
    public void appendBinaryData() {
        StepVerifier.create(fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();
                assertEquals(202, r.getStatusCode());
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertNotNull(headers.getValue(HttpHeaderName.DATE));
                assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void appendBinaryDataFlush() {
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions().setFlush(true);

        StepVerifier.create(fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();
                assertEquals(202, r.getStatusCode());
                assertNotNull(headers.getValue(X_MS_REQUEST_ID));
                assertNotNull(headers.getValue(X_MS_VERSION));
                assertNotNull(headers.getValue(HttpHeaderName.DATE));
                assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();
    }

    @Test
    public void flushDataMin() {
        Mono<PathInfo> response = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true));

        StepVerifier.create(response)
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void flushClose() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        Mono<Response<PathInfo>> response = fc.create()
            .then(fc.append(DATA.getDefaultBinaryData(), 0))
            .then(fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false,
                true, null, null));

        StepVerifier.create(response)
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void flushRetainUncommittedData() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        Mono<Response<PathInfo>> response = fc.create()
            .then(fc.append(DATA.getDefaultBinaryData(), 0))
            .then(fc.flushWithResponse(DATA.getDefaultDataSizeLong(), true,
                false, null, null));

        StepVerifier.create(response)
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void flushIA() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        Mono<Response<PathInfo>> response = fc.create()
            .then(fc.append(DATA.getDefaultBinaryData(), 0))
            .then(fc.flushWithResponse(4, false, false, null,
                null));

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null", "control,disposition,encoding,language,type"})
    public void flushHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                             String contentLanguage, String contentType) {
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);

        Mono<Response<PathProperties>> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> fc.append(DATA.getDefaultBinaryData(), 0)
                .then(fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false, false, headers,
                    null)
                .then(fc.getPropertiesWithResponse(null))));

        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(response)
            .assertNext(r -> validatePathProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                null, finalContentType))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void flushAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                        String leaseID) {
        Mono<Response<PathInfo>> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> r.append(DATA.getDefaultBinaryData(), 0)
                .then(Mono.zip(setupPathLeaseConditionAsync(r, leaseID),
                    setupPathMatchConditionAsync(r, match), Mono.just(r))))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                    .setLeaseId(newLease)
                    .setIfMatch(newMatch)
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified);
                return tuple.getT3().flushWithResponse(DATA.getDefaultDataSizeLong(), false,
                    false, null, drc);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void flushACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                            String leaseID) {
        Mono<Response<PathInfo>> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> r.append(DATA.getDefaultBinaryData(), 0)
                .then(Mono.zip(setupPathLeaseConditionAsync(r, leaseID), setupPathMatchConditionAsync(r, noneMatch),
                    Mono.just(r))))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                    .setLeaseId(leaseID)
                    .setIfMatch(match)
                    .setIfNoneMatch(newNoneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified);
                return tuple.getT3().flushWithResponse(DATA.getDefaultDataSize(), false, false,
                    null, drc);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);

    }

    @Test
    public void flushError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fc.flush(1, true))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void flushDataOverwrite() {
        Mono<PathInfo> response1 = fc.append(DATA.getDefaultBinaryData(), 0)
                .then(fc.flush(DATA.getDefaultDataSizeLong(), true));

        StepVerifier.create((response1))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        Mono<PathInfo> response2 = fc.append(DATA.getDefaultBinaryData(), 0)
            .then(fc.flush(DATA.getDefaultDataSizeLong(), false));
        // Attempt to write data without overwrite enabled
        StepVerifier.create(response2)
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @CsvSource({"file,file", "path/to]a file,path/to]a file", "path%2Fto%5Da%20file,path%2Fto%5Da%20file", "斑點,斑點",
        "%E6%96%91%E9%BB%9E,%E6%96%91%E9%BB%9E"})
    public void getFileNameAndBuildClient(String originalFileName, String finalFileName) {
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.getFileAsyncClient(originalFileName);

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
            .buildFileAsyncClient());
    }

    // "No overwrite interrupted" tests were not ported over for datalake. This is because the access condition check
    // occurs on the create method, so simple access conditions tests suffice.
    @LiveOnly // Test uploads large amount of data
    @ParameterizedTest
    @MethodSource("uploadFromFileSupplier")
    public void uploadFromFile(int fileSize, Long blockSize) throws IOException {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        // Block length will be ignored for single shot.
        StepVerifier.create(fac.uploadFromFile(file.getPath(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize), null, null, null))
            .verifyComplete();

        File outFile = new File(file.getPath() + "result");
        assertTrue(outFile.createNewFile());
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        StepVerifier.create(fac.readToFile(outFile.getPath(), true))
            .expectNextCount(1)
            .verifyComplete();

        compareFiles(file, outFile, 0, fileSize);
    }

    private static Stream<Arguments> uploadFromFileSupplier() {
        return Stream.of(
            // fileSize | blockSize
            Arguments.of(10, null), // Size is too small to trigger block uploading
            Arguments.of(10 * Constants.KB, null), // Size is too small to trigger block uploading
            Arguments.of(50 * Constants.MB, null), // Size is too small to trigger block uploading
            Arguments.of(101 * Constants.MB, 4L * 1024 * 1024) // Size is too small to trigger block uploading
        );
    }

    @Test
    public void uploadFromFileWithMetadata() {
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        File file = getRandomFile(Constants.KB);
        file.deleteOnExit();
        createdFiles.add(file);

        StepVerifier.create(fc.uploadFromFile(file.getPath(), null, null, metadata,
            null)
            .then(fc.getProperties()))
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fc.read()))
            .assertNext(r -> {
                try {
                    TestUtils.assertArraysEqual(Files.readAllBytes(file.toPath()), r);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    @Test
    public void uploadFromFileDefaultNoOverwrite() {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString()))
            .verifyError(DataLakeStorageException.class);

        Mono<Void> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> r.uploadFromFile(getRandomFile(50).toPath().toString()));

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void uploadFromFileOverwrite() {
        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(), true))
            .verifyComplete();

        Mono<Void> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> r.uploadFromFile(getRandomFile(50).toPath().toString(), true));

        StepVerifier.create(response)
            .verifyComplete();
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

    private static final class FileUploadListener implements ProgressListener {
        private long reportedByteCount;

        @Override
        public void handleProgress(long bytesTransferred) {
            this.reportedByteCount = bytesTransferred;
        }

        long getReportedByteCount() {
            return this.reportedByteCount;
        }
    }

    @SuppressWarnings("deprecation")
    @LiveOnly
    @ParameterizedTest
    @MethodSource("uploadFromFileWithProgressSupplier")
    public void uploadFromFileReporter(int size, long blockSize, int bufferCount) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        FileAsyncApiTests.FileUploadReporter uploadReporter = new FileAsyncApiTests.FileUploadReporter();

        File file = getRandomFile(size);
        file.deleteOnExit();
        createdFiles.add(file);

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize)
            .setMaxConcurrency(bufferCount)
            .setProgressReceiver(uploadReporter)
            .setMaxSingleUploadSizeLong(blockSize - 1);

        StepVerifier.create(fac.uploadFromFile(file.toPath().toString(), parallelTransferOptions, null,
            null, null))
            .verifyComplete();

        assertEquals(size, uploadReporter.getReportedByteCount());
    }

    private static Stream<Arguments> uploadFromFileWithProgressSupplier() {
        return Stream.of(
            // size | blockSize | bufferCount
            Arguments.of(10 * Constants.MB, 10L * Constants.MB, 8),
            Arguments.of(20 * Constants.MB, (long) Constants.MB, 5),
            Arguments.of(10 * Constants.MB, 5L * Constants.MB, 2),
            Arguments.of(10 * Constants.MB, 10L * Constants.KB, 100)
        );
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("uploadFromFileWithProgressSupplier")
    public void uploadFromFileListener(int size, long blockSize, int bufferCount) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        FileAsyncApiTests.FileUploadListener uploadListener = new FileAsyncApiTests.FileUploadListener();

        File file = getRandomFile(size);
        file.deleteOnExit();
        createdFiles.add(file);

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize)
            .setMaxConcurrency(bufferCount)
            .setProgressListener(uploadListener)
            .setMaxSingleUploadSizeLong(blockSize - 1);

        StepVerifier.create(fac.uploadFromFile(file.toPath().toString(), parallelTransferOptions, null,
            null, null))
            .verifyComplete();

        assertEquals(size, uploadListener.getReportedByteCount());
    }

    @ParameterizedTest
    @MethodSource("uploadFromFileOptionsSupplier")
    public void uploadFromFileOptions(int dataSize, long singleUploadSize, Long blockSize) {
        File file = getRandomFile(dataSize);
        file.deleteOnExit();
        createdFiles.add(file);


        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize),
            null, null, null).then(fc.getProperties()))
            .assertNext(r -> assertEquals(dataSize, r.getFileSize()))
            .verifyComplete();
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

        StepVerifier.create(fc.uploadFromFileWithResponse(file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize),
            null, null, null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                assertNotNull(r.getValue().getETag());
                assertNotNull(r.getValue().getLastModified());
            })
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> assertEquals(dataSize, r.getFileSize()))
            .verifyComplete();
    }

    @Test
    public void uploadFromFileEmptyFile() {
        File file = getRandomFile(0);
        file.deleteOnExit();
        createdFiles.add(file);

        StepVerifier.create(fc.uploadFromFileWithResponse(file.toPath().toString(), null, null,
            null, null))
            .assertNext(r -> {
                // uploadFromFileWithResponse will return 200 for a non-empty file, but since we are uploading an empty
                // file, it will return 201 since only createWithResponse gets called
                assertEquals(201, r.getStatusCode());
                assertNotNull(r.getValue().getETag());
            })
            .verifyComplete();

        StepVerifier.create(fc.getProperties())
            .assertNext(r -> assertEquals(0, r.getFileSize()))
            .verifyComplete();
    }

    @LiveOnly
    @Test
    public void asyncBufferedUploadEmpty() {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fac.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null))
            .verifyError(DataLakeStorageException.class);
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("asyncBufferedUploadEmptyBuffersSupplier")
    public void asyncBufferedUploadEmptyBuffers(ByteBuffer buffer1, ByteBuffer buffer2, ByteBuffer buffer3,
                                                byte[] expectedDownload) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fac.upload(Flux.fromIterable(Arrays.asList(buffer1, buffer2, buffer3)),
            null, true))
            .assertNext(pathInfo -> assertNotNull(pathInfo.getETag()))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fac.read()))
            .assertNext(bytes -> TestUtils.assertArraysEqual(expectedDownload, bytes))
            .verifyComplete();
    }

    private static Stream<Arguments> asyncBufferedUploadEmptyBuffersSupplier() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        byte[] helloBytes = "Hello".getBytes(StandardCharsets.UTF_8);
        byte[] worldBytes = "world!".getBytes(StandardCharsets.UTF_8);

        return Stream.of(
            // buffer1 | buffer2 | buffer3 || expectedDownload
            Arguments.of(ByteBuffer.wrap(helloBytes), ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)), ByteBuffer.wrap(worldBytes), "Hello world!".getBytes(StandardCharsets.UTF_8)),
            Arguments.of(ByteBuffer.wrap(helloBytes), ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)), emptyBuffer, "Hello ".getBytes(StandardCharsets.UTF_8)),
            Arguments.of(ByteBuffer.wrap(helloBytes), emptyBuffer, ByteBuffer.wrap(worldBytes), "Helloworld!".getBytes(StandardCharsets.UTF_8)),
            Arguments.of(emptyBuffer, ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)), ByteBuffer.wrap(worldBytes), " world!".getBytes(StandardCharsets.UTF_8))
        );
    }

    @LiveOnly // Test uploads large amount of data
    @ParameterizedTest
    @MethodSource("asyncBufferedUploadSupplier")
    public void asyncBufferedUpload(int dataSize, long bufferSize, int numBuffs) {
        byte[] data = getRandomByteArray(dataSize);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(bufferSize)
            .setMaxConcurrency(numBuffs)
            .setMaxSingleUploadSizeLong(4L * Constants.MB);

        Mono<byte[]> response = getPrimaryServiceClientForWrites(bufferSize)
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createFile(generatePathName())
            .flatMap(r -> {
                DataLakeFileAsyncClient facRead = dataLakeFileSystemAsyncClient.getFileAsyncClient(r.getFileName());
                return r.upload(Flux.just(ByteBuffer.wrap(data)), parallelTransferOptions, true)
                    .then(FluxUtil.collectBytesInByteBufferStream(facRead.read(), dataSize));
            });

        // Due to memory issues, this check only runs on small to medium-sized data sets.
        if (dataSize < 100 * 1024 * 1024) {
            StepVerifier.create(response)
                .assertNext(bytes -> TestUtils.assertArraysEqual(data, bytes))
                .verifyComplete();
        }
    }

    private static Stream<Arguments> asyncBufferedUploadSupplier() {
        return Stream.of(
            // dataSize | bufferSize | numBuffs || blockCount
            Arguments.of(35 * Constants.MB, 5L * Constants.MB, 2), // Requires cycling through the same buffers multiple times.
            Arguments.of(35 * Constants.MB, 5L * Constants.MB, 5), // Most buffers may only be used once.
            Arguments.of(100 * Constants.MB, 10L * Constants.MB, 2), // Larger data set.
            Arguments.of(100 * Constants.MB, 10L * Constants.MB, 5), // Larger number of Buffs.
            Arguments.of(10 * Constants.MB, (long) Constants.MB, 10), // Exactly enough buffer space to hold all the data.
            Arguments.of(50 * Constants.MB, 10L * Constants.MB, 2), // Larger data.
            Arguments.of(10 * Constants.MB, 2L * Constants.MB, 4),
            Arguments.of(10 * Constants.MB, 3L * Constants.MB, 3) // Data does not squarely fit in buffers.
        );
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

    private static final class Listener implements ProgressListener {
        private final long blockSize;
        private long reportingCount;

        Listener(long blockSize) {
            this.blockSize = blockSize;
        }

        @Override
        public void handleProgress(long bytesTransferred) {
            assert bytesTransferred % blockSize == 0;
            this.reportingCount += 1;
        }
    }

    @SuppressWarnings("deprecation")
    @LiveOnly
    @ParameterizedTest
    @MethodSource("bufferedUploadWithProgressSupplier")
    public void bufferedUploadWithReporter(int size, long blockSize, int bufferCount) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        FileAsyncApiTests.Reporter uploadReporter = new FileAsyncApiTests.Reporter(blockSize);

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize)
            .setMaxConcurrency(bufferCount)
            .setProgressReceiver(uploadReporter)
            .setMaxSingleUploadSizeLong(4L * Constants.MB);


        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(size)), parallelTransferOptions, null,
            null, null))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                // Verify that the reporting count is equal or greater than the size divided by block size in the case
                // that operations need to be retried. Retry attempts will increment the reporting count.
                assertTrue(uploadReporter.reportingCount >= (size / blockSize));
            })
            .verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadWithProgressSupplier() {
        return Stream.of(
            // size | blockSize | bufferCount
            Arguments.of(10 * Constants.MB, 10L * Constants.MB, 8),
            Arguments.of(20 * Constants.MB, (long) Constants.MB, 5),
            Arguments.of(10 * Constants.MB, 5L * Constants.MB, 2),
            Arguments.of(10 * Constants.MB, 512L * Constants.KB, 20)
        );
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("bufferedUploadWithProgressSupplier")
    public void bufferedUploadWithListener(int size, long blockSize, int bufferCount) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        FileAsyncApiTests.Listener uploadListener = new FileAsyncApiTests.Listener(blockSize);

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize)
            .setMaxConcurrency(bufferCount)
            .setProgressListener(uploadListener)
            .setMaxSingleUploadSizeLong(4L * Constants.MB);

        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(size)), parallelTransferOptions, null,
            null, null))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                // Verify that the reporting count is equal or greater than the size divided by block size in the case
                // that operations need to be retried. Retry attempts will increment the reporting count.
                assertTrue(uploadListener.reportingCount >= (size / blockSize));
            })
            .verifyComplete();
    }

    @LiveOnly // Test uploads large amount of data
    @ParameterizedTest
    @MethodSource("bufferedUploadChunkedSourceSupplier")
    public void bufferedUploadChunkedSource(List<Integer> dataSizeList, long bufferSize, int numBuffers) {
        // This test should validate that the upload should work regardless of what format the passed data is in because
        // it will be chunked appropriately.
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(bufferSize * Constants.MB)
            .setMaxConcurrency(numBuffers)
            .setMaxSingleUploadSizeLong(4L * Constants.MB);
        List<ByteBuffer> dataList = dataSizeList.stream()
            .map(size -> getRandomData(size * Constants.MB))
            .collect(Collectors.toList());

        Mono<byte[]> uploadOperation = getPrimaryServiceClientForWrites(bufferSize)
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createFile(generatePathName())
            .flatMap(r -> {
                DataLakeFileAsyncClient facRead = dataLakeFileSystemAsyncClient.getFileAsyncClient(r.getFileName());
                return r.upload(Flux.fromIterable(dataList), parallelTransferOptions, true)
                        .then(FluxUtil.collectBytesInByteBufferStream(facRead.read()));
            });

        StepVerifier.create(uploadOperation)
            .assertNext(bytes -> compareListToBuffer(dataList, ByteBuffer.wrap(bytes)))
            .verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadChunkedSourceSupplier() {
        return Stream.of(
            // dataSizeList | bufferSize | numBuffers
            Arguments.of(Arrays.asList(7, 7), 10L, 2), // First item fits entirely in the buffer, next item spans two buffers
            Arguments.of(Arrays.asList(3, 3, 3, 3, 3, 3, 3), 10L, 2), // Multiple items fit non-exactly in one buffer.
            Arguments.of(Arrays.asList(10, 10), 10L, 2), // Data fits exactly and does not need chunking.
            Arguments.of(Arrays.asList(50, 51, 49), 10L, 2) // Data needs chunking and does not fit neatly in buffers. Requires waiting for buffers to be released.
        );
    }

    // These two tests are to test optimizations in buffered upload for small files.
    @LiveOnly
    @ParameterizedTest
    @MethodSource("bufferedUploadHandlePathingSupplier")
    public void bufferedUploadHandlePathing(List<Integer> dataSizeList) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        List<ByteBuffer> dataList = dataSizeList.stream().map(this::getRandomData).collect(Collectors.toList());

        Mono<byte[]> uploadOperation = fac.upload(Flux.fromIterable(dataList),
                new ParallelTransferOptions().setMaxSingleUploadSizeLong(4L * Constants.MB), true)
            .then(FluxUtil.collectBytesInByteBufferStream(fac.read()));

        StepVerifier.create(uploadOperation)
            .assertNext(bytes -> compareListToBuffer(dataList, ByteBuffer.wrap(bytes)))
            .verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("bufferedUploadHandlePathingSupplier")
    public void bufferedUploadHandlePathingHotFlux(List<Integer> dataSizeList) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        List<ByteBuffer> dataList = dataSizeList.stream().map(this::getRandomData).collect(Collectors.toList());

        Mono<byte[]> uploadOperation = fac.upload(Flux.fromIterable(dataList).publish().autoConnect(),
                new ParallelTransferOptions().setMaxSingleUploadSizeLong(4L * Constants.MB), true)
            .then(FluxUtil.collectBytesInByteBufferStream(fac.read()));

        StepVerifier.create(uploadOperation)
            .assertNext(bytes -> compareListToBuffer(dataList, ByteBuffer.wrap(bytes)))
            .verifyComplete();
    }

    private static Stream<List<Integer>> bufferedUploadHandlePathingSupplier() {
        return Stream.of(Arrays.asList(10, 100, 1000, 10000), Arrays.asList(4 * Constants.MB + 1, 10),
            Arrays.asList(4 * Constants.MB, 4 * Constants.MB), Collections.singletonList(4 * Constants.MB));
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("bufferedUploadHandlePathingHotFluxWithTransientFailureSupplier")
    public void bufferedUploadHandlePathingHotFluxWithTransientFailure(List<Integer> dataSizeList) {
        DataLakeFileAsyncClient clientWithFailure = getFileAsyncClient(getDataLakeCredential(), fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());
        List<ByteBuffer> dataList = dataSizeList.stream().map(this::getRandomData).collect(Collectors.toList());

        DataLakeFileAsyncClient fcAsync = getFileAsyncClient(getDataLakeCredential(), fc.getFileUrl());

        Mono<byte[]> uploadOperation = clientWithFailure.upload(Flux.fromIterable(dataList).publish().autoConnect(),
                new ParallelTransferOptions().setMaxSingleUploadSizeLong(4L * Constants.MB), true)
            .then(FluxUtil.collectBytesInByteBufferStream(fcAsync.read()));

        StepVerifier.create(uploadOperation)
            .assertNext(bytes -> compareListToBuffer(dataList, ByteBuffer.wrap(bytes)))
            .verifyComplete();
    }

    private static Stream<List<Integer>> bufferedUploadHandlePathingHotFluxWithTransientFailureSupplier() {
        return Stream.of(Arrays.asList(10, 100, 1000, 10000), Arrays.asList(4 * Constants.MB + 1, 10),
            Arrays.asList(4 * Constants.MB, 4 * Constants.MB));
    }

    @SuppressWarnings("deprecation")
    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {11110, 2 * Constants.MB + 11})
    public void bufferedUploadAsyncHandlePathingWithTransientFailure(int dataSize) {
        // This test ensures that although we no longer mark and reset the source stream for buffered upload, it still
        // supports retries in all cases for the sync client.
        DataLakeFileAsyncClient clientWithFailure = getFileAsyncClient(getDataLakeCredential(), fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());

        byte[] data = getRandomByteArray(dataSize);

        Mono<byte[]> response = clientWithFailure.uploadWithResponse(new FileParallelUploadOptions(new ByteArrayInputStream(data), dataSize)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(2L * Constants.MB)
                .setBlockSizeLong(2L * Constants.MB))).then(FluxUtil.collectBytesInByteBufferStream(fc.read()));

        StepVerifier.create(response)
            .assertNext(r -> TestUtils.assertArraysEqual(data, r))
            .verifyComplete();
    }

    @Test
    public void bufferedUploadIllegalArgumentsNull() {
        Mono<PathInfo> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(r -> r.upload((Flux<ByteBuffer>) null,
                    new ParallelTransferOptions().setBlockSizeLong(4L).setMaxConcurrency(4), true));

        StepVerifier.create(response)
            .verifyError(NullPointerException.class);
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("bufferedUploadHeadersSupplier")
    public void bufferedUploadHeaders(int dataSize, String cacheControl, String contentDisposition,
                                      String contentEncoding, String contentLanguage, boolean validateContentMD5, String contentType)
        throws NoSuchAlgorithmException {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        byte[] randomData = getRandomByteArray(dataSize);
        byte[] contentMD5 = validateContentMD5 ? MessageDigest.getInstance("MD5").digest(randomData) : null;
        Mono<Response<PathProperties>> uploadOperation = fac
            .uploadWithResponse(Flux.just(ByteBuffer.wrap(randomData)),
                new ParallelTransferOptions().setMaxSingleUploadSizeLong(4L * Constants.MB),
                new PathHttpHeaders()
                .setCacheControl(cacheControl)
                .setContentDisposition(contentDisposition)
                .setContentEncoding(contentEncoding)
                .setContentLanguage(contentLanguage)
                .setContentMd5(contentMD5)
                .setContentType(contentType), null, null)
            .then(fac.getPropertiesWithResponse(null));

        StepVerifier.create(uploadOperation)
            .assertNext(response -> validatePathProperties(response, cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType == null ? "application/octet-stream" : contentType))
            .verifyComplete();
    }

    private static Stream<Arguments> bufferedUploadHeadersSupplier() {
        return Stream.of(
            // dataSize | cacheControl | contentDisposition | contentEncoding | contentLanguage | validateContentMD5 | contentType
            Arguments.of(DATA.getDefaultDataSize(), null, null, null, null, true, null),
            Arguments.of(DATA.getDefaultDataSize(), "control", "disposition", "encoding", "language", true, "type"),
            Arguments.of(6 * Constants.MB, null, null, null, null, false, null),
            Arguments.of(6 * Constants.MB, "control", "disposition", "encoding", "language", true, "type")
        );
    }

    @LiveOnly
    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void bufferedUploadMetadata(String key1, String value1, String key2, String value2) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10L)
            .setMaxConcurrency(10);
        Mono<Response<PathProperties>> uploadOperation = fac.uploadWithResponse(Flux.just(getRandomData(10)),
                parallelTransferOptions, null, metadata, null)
            .then(fac.getPropertiesWithResponse(null));

        StepVerifier.create(uploadOperation)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(metadata, response.getValue().getMetadata());
            })
            .verifyComplete();
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
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize),
            null, null, null))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fac.getProperties())
            .assertNext(properties -> assertEquals(dataSize, properties.getFileSize()))
            .verifyComplete();

        assertEquals(numAppends, appendCount.get());
    }

    @Test
    public void bufferedUploadPermissionsAndUmask() {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        Mono<Response<PathProperties>> uploadOperation = fac.uploadWithResponse(
            new FileParallelUploadOptions(Flux.just(getRandomData(10))).setPermissions("0777").setUmask("0057"))
            .then(fac.getPropertiesWithResponse(null));

        StepVerifier.create(uploadOperation)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(10, response.getValue().getFileSize());
            })
            .verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void bufferedUploadAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        Mono<Response<PathInfo>> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(fac -> Mono.zip(setupPathLeaseConditionAsync(fac, leaseID), setupPathMatchConditionAsync(fac, match),
                Mono.just(fac)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10L);
                return tuple.getT3().uploadWithResponse(Flux.just(getRandomData(10)),
                        parallelTransferOptions, null, null, drc);
            });

        assertAsyncResponseStatusCode(response, 200);
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void bufferedUploadACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        Mono<Response<PathInfo>> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(fac -> Mono.zip(setupPathLeaseConditionAsync(fac, leaseID), setupPathMatchConditionAsync(fac, noneMatch),
                Mono.just(fac)))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10L);

                return tuple.getT3().uploadWithResponse(Flux.just(getRandomData(10)),
                        parallelTransferOptions, null, null, drc);
            });

        StepVerifier.create(response)
            .verifyErrorSatisfies(ex -> {
                DataLakeStorageException exception = assertInstanceOf(DataLakeStorageException.class, ex);
                assertEquals(412, exception.getStatusCode());
            });
    }

    // UploadBufferPool used to lock when the number of failed stageblocks exceeded the maximum number of buffers
    // (discovered when a leaseId was invalid)
    @LiveOnly
    @ParameterizedTest
    @CsvSource({"7,2", "5,2"})
    public void uploadBufferPoolLockThreeOrMoreBuffers(long blockSize, int numBuffers) {
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize)
            .setMaxConcurrency(numBuffers);

        Mono<Response<PathInfo>> response = dataLakeFileSystemAsyncClient.createFile(generatePathName())
            .flatMap(fac -> Mono.zip(setupPathLeaseConditionAsync(fac, GARBAGE_LEASE_ID), Mono.just(fac)))
            .flatMap(tuple -> {
                DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().
                    setLeaseId(tuple.getT1());
                return tuple.getT2().uploadWithResponse(Flux.just(getRandomData(10)),
                    parallelTransferOptions, null, null, requestConditions);
            });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @LiveOnly
    @Test
    public void bufferedUploadDefaultNoOverwrite() {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fac.upload(DATA.getDefaultFlux(), null).then(fac.upload(DATA.getDefaultFlux(),
            null)))
            .verifyError(IllegalArgumentException.class);
    }

    @LiveOnly
    @Test
    public void bufferedUploadOverwrite() {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        File file = getRandomFile(50);
        file.deleteOnExit();
        createdFiles.add(file);

        StepVerifier.create(fc.uploadFromFile(file.toPath().toString(), true))
            .verifyComplete();

        StepVerifier.create(fac.uploadFromFile(getRandomFile(50).toPath().toString(), true))
            .verifyComplete();
    }

    @Test
    public void bufferedUploadNonMarkableStream() throws IOException {
        File file = getRandomFile(10);
        file.deleteOnExit();
        createdFiles.add(file);

        File outFile = getRandomFile(10);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        Flux<ByteBuffer> stream = FluxUtil.readFile(AsynchronousFileChannel.open(file.toPath()), 0, file.length());

        StepVerifier.create(fc.upload(stream, null, true)
            .then(fc.readToFile(outFile.toPath().toString(), true)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        compareFiles(file, outFile, 0, file.length());
    }

    @Test
    public void uploadInputStreamNoLength() {
        StepVerifier.create(fc.uploadWithResponse(new FileParallelUploadOptions(DATA.getDefaultInputStream())))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fc.read()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("uploadInputStreamBadLengthSupplier")
    public void uploadInputStreamBadLength(long length) {
        assertThrows(Exception.class, () -> fc.uploadWithResponse(
            new FileParallelUploadOptions(DATA.getDefaultInputStream(), length)).block());
    }

    private static Stream<Long> uploadInputStreamBadLengthSupplier() {
        return Stream.of(0L, -100L, DATA.getDefaultDataSizeLong() - 1, DATA.getDefaultDataSizeLong() + 1);
    }

    @Test
    public void uploadSuccessfulRetry() {
        DataLakeFileAsyncClient clientWithFailure = getFileAsyncClient(getDataLakeCredential(), fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());

        StepVerifier.create(clientWithFailure.uploadWithResponse(
            new FileParallelUploadOptions(DATA.getDefaultInputStream())))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fc.read()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @Test
    public void uploadBinaryData() {
        DataLakeFileAsyncClient client = getFileAsyncClient(getDataLakeCredential(), fc.getFileUrl());

        StepVerifier.create(client.uploadWithResponse(new FileParallelUploadOptions(DATA.getDefaultBinaryData())))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fc.read()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @Test
    public void uploadBinaryDataOverwrite() {
        DataLakeFileAsyncClient client = getFileAsyncClient(getDataLakeCredential(), fc.getFileUrl());

        StepVerifier.create(client.upload(DATA.getDefaultBinaryData(), null, true))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fc.read()))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2021-04-10")
    @Test
    public void uploadEncryptionContext() {
        String encryptionContext = "encryptionContext";
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultInputStream())
            .setEncryptionContext(encryptionContext);

        StepVerifier.create(fc.uploadWithResponse(options).then(fc.getProperties()))
            .assertNext(r -> assertEquals(encryptionContext, r.getEncryptionContext()))
            .verifyComplete();
    }

    /* Quick Query Tests. */

    // Generates and uploads a CSV file
    private Mono<PathInfo> uploadCsv(FileQueryDelimitedSerialization s, int numCopies) {
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

        return fc.create(true).then(fc.append(BinaryData.fromBytes(data), 0)).then(fc.flush(data.length,
            true));
    }

    private Mono<PathInfo> uploadSmallJson(int numCopies) {
        StringBuilder b = new StringBuilder();
        b.append("{\n");
        for (int i = 0; i < numCopies; i++) {
            b.append(String.format("\t\"name%d\": \"owner%d\",\n", i, i));
        }
        b.append('}');

        return fc.create(true).then(fc.append(BinaryData.fromString(b.toString()), 0)).then(fc.flush(b.length(),
            true));
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
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {

            Mono<byte[]> readArray = FluxUtil.collectBytesInByteBufferStream(fc.read());

            Mono<ByteArrayOutputStream> queryResponse = fc.query(expression).reduce(new ByteArrayOutputStream(),
                (outputStream, piece) -> {
                    try {
                        outputStream.write(piece.array());
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                    return outputStream;
                });

            StepVerifier.create(uploadCsv(ser, numCopies).then(Mono.zip(readArray, queryResponse)))
                .assertNext(r -> TestUtils.assertArraysEqual(r.getT1(), r.getT2().toByteArray()))
                .verifyComplete();
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
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            ByteArrayOutputStream queryData = new ByteArrayOutputStream();

            Mono<byte[]> readArray = FluxUtil.collectBytesInByteBufferStream(fc.read());

            Mono<byte[]> queryArray = fc.queryWithResponse(new FileQueryOptions(expression, queryData)
                    .setInputSerialization(serIn).setOutputSerialization(serOut))
                .flatMap(piece -> FluxUtil.collectBytesInByteBufferStream(piece.getValue()));

            StepVerifier.create(uploadCsv(serIn, 32).then(Mono.zip(readArray, queryArray)))
                .assertNext(r -> {
                    if (headersPresentIn && !headersPresentOut) {
                        assertEquals(r.getT1().length - 16, r.getT2().length);

                        /* Account for 16 bytes of header. */
                        TestUtils.assertArraysEqual(r.getT1(), 16, r.getT2(), 0, r.getT1().length - 16);
                    } else {
                        TestUtils.assertArraysEqual(r.getT1(), r.getT2());
                    }
                })
                .verifyComplete();
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

        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            ByteArrayOutputStream queryData = new ByteArrayOutputStream();

            Mono<byte[]> readArray = FluxUtil.collectBytesInByteBufferStream(fc.read());

            Mono<byte[]> queryArray = fc.queryWithResponse(new FileQueryOptions(expression, queryData)
                .setInputSerialization(ser).setOutputSerialization(ser))
                .flatMap(piece -> FluxUtil.collectBytesInByteBufferStream(piece.getValue()));

            StepVerifier.create(uploadCsv(ser, 32).then(Mono.zip(readArray, queryArray)))
                .assertNext(r -> TestUtils.assertArraysEqual(r.getT1(), r.getT2()))
                .verifyComplete();
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("queryInputJsonSupplier")
    public void queryInputJson(int numCopies, char recordSeparator) {
        FileQueryJsonSerialization ser = new FileQueryJsonSerialization()
            .setRecordSeparator(recordSeparator);
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            ByteArrayOutputStream readData = new ByteArrayOutputStream();
            Mono<Void> read = uploadSmallJson(numCopies)
                .then(FluxUtil.writeToOutputStream(fc.read(), readData));

            ByteArrayOutputStream queryData = new ByteArrayOutputStream();
            FileQueryOptions optionsOs = new FileQueryOptions(expression, queryData)
                .setInputSerialization(ser).setOutputSerialization(ser);

            Mono<byte[]> queryArray = fc.queryWithResponse(optionsOs)
                .flatMap(piece -> FluxUtil.collectBytesInByteBufferStream(piece.getValue()));

            StepVerifier.create(read.then(queryArray))
                .assertNext(r -> {
                    readData.write(10);
                    byte[] readArray = readData.toByteArray();
                    TestUtils.assertArraysEqual(readArray, r);
                })
                .verifyComplete();
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
            FileQueryJsonSerialization outSer = new FileQueryJsonSerialization().setRecordSeparator('\n');
            String expression = "SELECT * from BlobStorage";
            byte[] expectedData = "{\"_1\":\"100\",\"_2\":\"200\",\"_3\":\"300\",\"_4\":\"400\"}".getBytes();

            ByteArrayOutputStream queryData = new ByteArrayOutputStream();
            FileQueryOptions optionsOs = new FileQueryOptions(expression, queryData).setInputSerialization(inSer)
                .setOutputSerialization(outSer);

            StepVerifier.create(uploadCsv(inSer, 1).then(fc.queryWithResponse(optionsOs)
                .flatMap(piece -> FluxUtil.collectBytesInByteBufferStream(piece.getValue()))))
                .assertNext(r -> TestUtils.assertArraysEqual(expectedData, 0, r, 0, expectedData.length))
                .verifyComplete();
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryInputJsonOutputCsv() {
        liveTestScenarioWithRetry(() -> {
            FileQueryJsonSerialization inSer = new FileQueryJsonSerialization().setRecordSeparator('\n');

            FileQueryDelimitedSerialization outSer = new FileQueryDelimitedSerialization()
                .setRecordSeparator('\n')
                .setColumnSeparator(',')
                .setEscapeChar('\0')
                .setFieldQuote('\0')
                .setHeadersPresent(false);
            String expression = "SELECT * from BlobStorage";
            byte[] expectedData = "owner0,owner1\n".getBytes();

            ByteArrayOutputStream queryData = new ByteArrayOutputStream();
            FileQueryOptions optionsOs = new FileQueryOptions(expression, queryData).setInputSerialization(inSer)
                .setOutputSerialization(outSer);

            StepVerifier.create(uploadSmallJson(2).then(fc.queryWithResponse(optionsOs)
                .flatMap(piece -> FluxUtil.collectBytesInByteBufferStream(piece.getValue()))))
                .assertNext(r -> TestUtils.assertArraysEqual(expectedData, r))
                .verifyComplete();
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
        List<FileQueryArrowField> schema = Collections.singletonList(
            new FileQueryArrowField(FileQueryArrowFieldType.DECIMAL).setName("Name").setPrecision(4).setScale(2));
        FileQueryArrowSerialization outSer = new FileQueryArrowSerialization().setSchema(schema);
        String expression = "SELECT _2 from BlobStorage WHERE _1 > 250;";

        liveTestScenarioWithRetry(() -> {
            OutputStream queryData = new ByteArrayOutputStream();
            FileQueryOptions options = new FileQueryOptions(expression, queryData).setOutputSerialization(outSer);

            StepVerifier.create(uploadCsv(inSer, 32).then(fc.queryWithResponse(options)))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();
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
        String expression = "SELECT _1 from BlobStorage WHERE _2 > 250";

        liveTestScenarioWithRetry(() -> {
            MockErrorReceiver receiver2 = new FileAsyncApiTests.MockErrorReceiver("InvalidColumnOrdinal");

            StepVerifier.create(uploadCsv(base.setColumnSeparator('.'), 32)
                .thenMany(fc.queryWithResponse(new FileQueryOptions(expression, new ByteArrayOutputStream())
                .setInputSerialization(base.setColumnSeparator(','))
                .setOutputSerialization(base.setColumnSeparator(','))
                .setErrorConsumer(receiver2)).flatMapMany(r -> r.getValue())))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

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
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            StepVerifier.create(uploadCsv(base.setColumnSeparator('.'), 32)
                .then(fc.queryWithResponse(new FileQueryOptions(expression, new ByteArrayOutputStream())
                .setInputSerialization(new FileQueryJsonSerialization()))).flatMapMany(r -> r.getValue().thenMany(r.getValue())))
                .verifyError(IOException.class);
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

        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            MockProgressReceiver mockReceiver = new com.azure.storage.file.datalake.FileAsyncApiTests.MockProgressReceiver();
            FileQueryOptions options = new FileQueryOptions(expression, new ByteArrayOutputStream()).
                setProgressConsumer(mockReceiver);
            // The Avro stream has the following pattern
            // (data record -> progress record) -> end record
            //
            // 1KB of data will only come back as a single data record.
            //
            // Pretend to read more data because the input stream will not parse records following the data record if it
            // doesn't need to.
            StepVerifier.create(uploadCsv(base.setColumnSeparator('.'), 32)
                .then(fc.queryWithResponse(options))
                .flatMapMany(r -> r.getValue())
                .then(fc.getProperties()))
                .assertNext(r -> {
                    // At least the size of blob to read will be in the progress list
                    assertTrue(mockReceiver.progressList.contains(r.getFileSize()));
                })
                .verifyComplete();
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

        liveTestScenarioWithRetry(() -> {
            MockProgressReceiver mockReceiver = new com.azure.storage.file.datalake.FileAsyncApiTests.MockProgressReceiver();
            long temp = 0;
            FileQueryOptions options = new FileQueryOptions(expression, new ByteArrayOutputStream())
                .setProgressConsumer(mockReceiver);

            StepVerifier.create(uploadCsv(ser, 512000)
                .then(fc.queryWithResponse(options))
                .flatMapMany(r -> r.getValue()))
                .thenConsumeWhile(r -> true)
                .verifyComplete();

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
            StepVerifier.create(fc.queryWithResponse(
                new FileQueryOptions(expression, new ByteArrayOutputStream())
                .setInputSerialization(inSer)
                .setOutputSerialization(outSer)))
                .verifyError(IllegalArgumentException.class);
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryArrowInputIA() {
        FileQueryArrowSerialization inSer = new FileQueryArrowSerialization();
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            StepVerifier.create(fc.queryWithResponse(
                new FileQueryOptions(expression, new ByteArrayOutputStream()).setInputSerialization(inSer)))
                .verifyError(IllegalArgumentException.class);

        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-10-02")
    @Test
    public void queryParquetOutputIA() {
        FileQueryParquetSerialization outSer = new FileQueryParquetSerialization();
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            StepVerifier.create(fc.queryWithResponse(
                new FileQueryOptions(expression, new ByteArrayOutputStream()).setOutputSerialization(outSer)))
                .verifyError(IllegalArgumentException.class);
        });
    }

    @SuppressWarnings("resource")
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryError() {
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        liveTestScenarioWithRetry(() -> {
            StepVerifier.create(fc.query("SELECT * from BlobStorage"))
                .verifyError(DataLakeStorageException.class);
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void queryAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                        String leaseID) {
        String expression = "SELECT * from BlobStorage";
        Mono<FileQueryAsyncResponse> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, match))
            .flatMap(tuple -> {
                String newLease = tuple.getT1();
                String newMatch = tuple.getT2();
                if ("null".equals(newLease)) {
                    newLease = null;
                }
                if ("null".equals(newMatch)) {
                    newMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(newLease)
                        .setIfMatch(newMatch)
                        .setIfNoneMatch(noneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.queryWithResponse(new FileQueryOptions(expression, new ByteArrayOutputStream())
                        .setRequestConditions(drc));
            });
        liveTestScenarioWithRetry(() -> {
            StepVerifier.create(response)
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();
        });
    }

    private void liveTestScenarioWithRetry(Runnable runnable) {
        int retry = 0;
        while (retry < 5) {
            try {
                runnable.run();
                break;
            } catch (Exception ex) {
                retry++;
                sleepIfLiveTesting(5000);
            }
        }
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void queryACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                            String leaseID) {
        String expression = "SELECT * from BlobStorage";
        Mono<FileQueryAsyncResponse> response = Mono.zip(setupPathLeaseConditionAsync(fc, leaseID),
            setupPathMatchConditionAsync(fc, noneMatch))
            .flatMap(tuple -> {
                String newNoneMatch = tuple.getT2();
                if ("null".equals(newNoneMatch)) {
                    newNoneMatch = null;
                }
                DataLakeRequestConditions drc = new DataLakeRequestConditions()
                        .setLeaseId(leaseID)
                        .setIfMatch(match)
                        .setIfNoneMatch(newNoneMatch)
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified);
                return fc.queryWithResponse(new FileQueryOptions(expression, new ByteArrayOutputStream()).setRequestConditions(drc));
            });

        StepVerifier.create(response)
                .verifyError(DataLakeStorageException.class);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("scheduleDeletionSupplier")
    public void scheduleDeletion(FileScheduleDeletionOptions fileScheduleDeletionOptions, boolean hasExpiry) {
        DataLakeFileAsyncClient fileAsyncClient = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        Mono<PathProperties> response = fileAsyncClient.create()
            .then(fileAsyncClient.scheduleDeletionWithResponse(fileScheduleDeletionOptions))
            .then(fileAsyncClient.getProperties());

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(hasExpiry, r.getExpiresOn() != null))
            .verifyComplete();
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
        DataLakeFileAsyncClient fileAsyncClient = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        Mono<PathProperties> response = fileAsyncClient.create()
            .then(fileAsyncClient.scheduleDeletionWithResponse(fileScheduleDeletionOptions))
            .then(fileAsyncClient.getProperties());

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(now.plusDays(1).truncatedTo(ChronoUnit.SECONDS), r.getExpiresOn()))
            .verifyComplete();
    }

    @Test
    public void scheduleDeletionError() {
        FileScheduleDeletionOptions fileScheduleDeletionOptions = new FileScheduleDeletionOptions(testResourceNamer.now().plusDays(1));
        DataLakeFileAsyncClient fileAsyncClient = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        StepVerifier.create(fileAsyncClient.scheduleDeletionWithResponse(fileScheduleDeletionOptions))
            .verifyError(DataLakeStorageException.class);
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
        StepVerifier.create(fc.upload(DATA.getDefaultBinaryData(), null))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void uploadInputStreamOverwrite() {
        StepVerifier.create(fc.upload(DATA.getDefaultBinaryData(), null, true)
            .then(FluxUtil.collectBytesInByteBufferStream(fc.read())))
            .assertNext(r -> TestUtils.assertArraysEqual(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @LiveOnly /* Flaky in playback. */
    @Test
    public void uploadInputStreamLargeData() {
        ByteArrayInputStream input = new ByteArrayInputStream(getRandomByteArray(20 * Constants.MB));
        ParallelTransferOptions pto = new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB);

        // Uses blob output stream under the hood.
        StepVerifier.create(fc.uploadWithResponse(new FileParallelUploadOptions(input, 20 * Constants.MB)
            .setParallelTransferOptions(pto)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
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
            Arguments.of((100 * Constants.MB) + 1, null, null, (int) Math.ceil(((double) (100 * Constants.MB) + 1) / (double) (4 * Constants.MB))),
            Arguments.of(100, 50L, null, 1),
            Arguments.of(100, 50L, 20L, 5)
        );
    }

    @SuppressWarnings("deprecation")
    @Test
    public void uploadReturnValue() {
        StepVerifier.create(fc.uploadWithResponse(
            new FileParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong())))
            .assertNext(r -> assertNotNull(r.getValue().getETag()))
            .verifyComplete();
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        DataLakeFileAsyncClient fileAsyncClient = getPathClientBuilder(getDataLakeCredential(), fc.getFileUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildFileAsyncClient();

        // blob endpoint
        StepVerifier.create(fileAsyncClient.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals("2019-02-02", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();

        // dfs endpoint
        StepVerifier.create(fileAsyncClient.getAccessControlWithResponse(false, null))
            .assertNext(r -> assertEquals("2019-02-02", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    @Test
    public void defaultAudience() {
        DataLakeFileAsyncClient aadFileClient = getPathClientBuilderWithTokenCredential(
            dataLakeFileSystemAsyncClient.getFileSystemUrl(), fc.getFilePath())
            .fileSystemName(dataLakeFileSystemAsyncClient.getFileSystemName())
            .audience(null) // should default to "https://storage.azure.com/"
            .buildFileAsyncClient();

        StepVerifier.create(aadFileClient.exists())
            .expectNext(true)
            .verifyComplete();
    }


    @Test
    public void storageAccountAudience() {
        DataLakeFileAsyncClient aadFileClient = getPathClientBuilderWithTokenCredential(
            ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint(), fc.getFilePath())
            .fileSystemName(dataLakeFileSystemAsyncClient.getFileSystemName())
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience(dataLakeFileSystemAsyncClient.getAccountName()))
            .buildFileAsyncClient();

        StepVerifier.create(aadFileClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        DataLakeFileAsyncClient aadFileClient = getPathClientBuilderWithTokenCredential(
            ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint(), fc.getFilePath())
            .fileSystemName(dataLakeFileSystemAsyncClient.getFileSystemName())
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience("badAudience"))
            .buildFileAsyncClient();

        StepVerifier.create(aadFileClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", dataLakeFileSystemAsyncClient.getAccountName());
        DataLakeAudience audience = DataLakeAudience.fromString(url);

        DataLakeFileAsyncClient aadFileClient = getPathClientBuilderWithTokenCredential(
            ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint(), fc.getFilePath())
            .fileSystemName(dataLakeFileSystemAsyncClient.getFileSystemName())
            .audience(audience)
            .buildFileAsyncClient();

        StepVerifier.create(aadFileClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2024-05-04")
    @Test
    public void aclHeaderTests() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        dataLakeFileSystemClient.create();
        dataLakeFileSystemClient.getDirectoryClient(generatePathName()).create();
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES);

        //getProperties
        StepVerifier.create(fc.createWithResponse(options).then(fc.getProperties()))
            .assertNext(r -> assertTrue(PATH_ACCESS_CONTROL_ENTRIES.containsAll(r.getAccessControlList())))
            .verifyComplete();

        //readWithResponse
        StepVerifier.create(fc.readWithResponse(null, null, null, false))
            .assertNext(r -> assertTrue(PATH_ACCESS_CONTROL_ENTRIES.containsAll(r.getDeserializedHeaders().getAccessControlList())))
            .verifyComplete();

        //readToFileWithResponse
        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        StepVerifier.create(fc.readToFileWithResponse(outFile.getPath(), null,
            null, null, null, false, null))
            .assertNext(r -> assertTrue(PATH_ACCESS_CONTROL_ENTRIES.containsAll(r.getValue().getAccessControlList())))
            .verifyComplete();

    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2024-05-04")
    @ParameterizedTest
    @MethodSource("upnHeaderTestSupplier")
    public void upnHeaderTest(Boolean upnHeader) {
        //feature currently doesn't work in preprod - test uses methods that send the request header. verified in fiddler
        //that the header is being sent and is properly assigned.
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());
        dataLakeFileSystemAsyncClient.create().block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).create().block();
        fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());

        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES);
        fc.createWithResponse(options).block();

        //getProperties
        PathGetPropertiesOptions propertiesOptions = new PathGetPropertiesOptions().setUserPrincipalName(upnHeader);

        StepVerifier.create(fc.getProperties(propertiesOptions))
            .assertNext(r -> assertNotNull(r.getAccessControlList()))
            .verifyComplete();

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

        StepVerifier.create(fc.readToFile(readToFileOptions))
            .assertNext(r -> assertNotNull(r.getAccessControlList()))
            .verifyComplete();

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        StepVerifier.create(fc.readToFileWithResponse(readToFileOptions))
            .assertNext(r -> assertNotNull(r.getValue().getAccessControlList()))
            .verifyComplete();
    }

    private static Stream<Arguments> upnHeaderTestSupplier() {
        return Stream.of(
            Arguments.of(false),
            Arguments.of(true),
            Arguments.of((Boolean) null));
    }


}

