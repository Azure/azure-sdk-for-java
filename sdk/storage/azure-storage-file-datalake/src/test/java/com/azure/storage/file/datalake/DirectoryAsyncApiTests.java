package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PathRemoveAccessControlEntry;
import com.azure.storage.file.datalake.models.RolePermissions;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.options.DataLakePathScheduleDeletionOptions;
import com.azure.storage.file.datalake.options.FileSystemEncryptionScopeOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.PathSasPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectoryAsyncApiTests extends DataLakeTestBase {

    private static final PathPermissions PERMISSIONS = new PathPermissions()
        .setOwner(new RolePermissions().setReadPermission(true).setWritePermission(true).setExecutePermission(true))
        .setGroup(new RolePermissions().setReadPermission(true).setExecutePermission(true))
        .setOther(new RolePermissions().setReadPermission(true));
    private static final List<PathAccessControlEntry> PATH_ACCESS_CONTROL_ENTRIES =
        PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
    private static final List<PathAccessControlEntry> EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES =
        PathAccessControlEntry.parseList("user::--x,group::--x,other::--x");
    private static final List<PathRemoveAccessControlEntry> REMOVE_ACCESS_CONTROL_ENTRIES =
        PathRemoveAccessControlEntry.parseList(
            "mask,default:user,default:group,user:ec3595d6-2c17-4696-8caa-7e139758d24a,"
                + "group:ec3595d6-2c17-4696-8caa-7e139758d24a,"
                + "default:user:ec3595d6-2c17-4696-8caa-7e139758d24a,"
                + "default:group:ec3595d6-2c17-4696-8caa-7e139758d24a");
    private static final String GROUP = null;
    private static final String OWNER = null;

    private DataLakeDirectoryAsyncClient dc;

    @BeforeEach
    public void setup() {
        String directoryName = generatePathName();
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(directoryName);
        dc.create().block();
    }

    @Test
    public void createMin() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        assertDoesNotThrow(() -> dc.create().block());
    }

    @Test
    public void createDefaults() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName())
            .createWithResponse(null, null, null, null, null))
            .assertNext(r ->{
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createDefaultsWithOptions() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setMetadata(Collections.singletonMap("foo", "bar"))
            .setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES);

        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(dc.createWithResponse(options))
            .assertNext(r ->{
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();

        StepVerifier.create(dc.getAccessControl())
            .assertNext(r -> compareACL(PATH_ACCESS_CONTROL_ENTRIES, r.getAccessControlList()))
            .verifyComplete();
    }

    @Test
    public void createDefaultsWithNullOptions() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName())
            .createWithResponse(null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createError() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName())
            .createWithResponse(null, null, null, null,
                new DataLakeRequestConditions().setIfMatch("garbage")))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createOverwrite() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.create().block();

        StepVerifier.create(dc.create(false))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void exists() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.create().block();

        StepVerifier.create(dc.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void doesNotExist() {

        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).exists())
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    public void createHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                              String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.createWithResponse(null, null, headers, null, null).block();

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        String finalContentType = contentType;
        StepVerifier.create(dc.getPropertiesWithResponse(null))
            .assertNext(r -> validatePathProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage, null,
                finalContentType))
            .verifyComplete();
    }

    private static Stream<Arguments> cacheAndContentSupplier() {
        return Stream.of(
            // cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType
            Arguments.of(null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language", "type")
        );
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

        dc.createWithResponse(null, null, null, metadata, null).block();

        StepVerifier.create(dc.getProperties())
            .assertNext(r ->{
                for (String k : metadata.keySet()) {
                    assertTrue(r.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), r.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier") //todo: replace w assertAsyncResponseStatusCode
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);


        StepVerifier.create(dc.createWithResponse(null, null, null, null, drc))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();
        //replace
        //assertEquals(201, dc.createWithResponse(null, null, null, null, drc).getStatusCode());
    }

    private static Stream<Arguments> modifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified, unmodified, match, noneMatch, leaseID
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
        setupPathLeaseCondition(dc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.createWithResponse(null, null, null, null, drc))
            .verifyError(DataLakeStorageException.class);
    }

    private static Stream<Arguments> invalidModifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified, unmodified, match, noneMatch, leaseID
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID)
        );
    }

    @Test
    public void createPermissionsAndUmask() { //todo: replace w assertAsyncResponseStatusCode
       StepVerifier.create(dc.createWithResponse("0777", "0057", null, null, null))
           .assertNext(r -> assertEquals(201, r.getStatusCode()))
           .verifyComplete();
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

        StepVerifier.create(client.createDirectory(generatePathName())
            .flatMap(DataLakePathAsyncClient::getProperties))
            .assertNext(p -> assertEquals(ENCRYPTION_SCOPE_STRING, p.getEncryptionScope()))
            .verifyComplete();
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @Test
    public void createEncryptionScopeSas() {
        PathSasPermission permissions = new PathSasPermission()
            .setReadPermission(true)
            .setMovePermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setDeletePermission(true);

        String sas = dataLakeFileSystemAsyncClient.generateSas(new DataLakeServiceSasSignatureValues(
            testResourceNamer.now().plusDays(1), permissions).setEncryptionScope(ENCRYPTION_SCOPE_STRING));

        DataLakeDirectoryAsyncClient client = getDirectoryAsyncClient(sas, dataLakeFileSystemClient.getFileSystemUrl(),
            generatePathName());
        client.create().block();

        StepVerifier.create(client.getProperties())
            .assertNext(r -> assertEquals(ENCRYPTION_SCOPE_STRING, r.getEncryptionScope()))
            .verifyComplete();
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @Test
    public void createEncryptionScopeIdentitySas() { //todo: fix oauth
        UserDelegationKey key = getOAuthServiceClient()
            .getUserDelegationKey(null, testResourceNamer.now().plusHours(1));
        key.setSignedObjectId(testResourceNamer.recordValueFromConfig(key.getSignedObjectId()));
        key.setSignedTenantId(testResourceNamer.recordValueFromConfig(key.getSignedTenantId()));

        PathSasPermission permissions = new PathSasPermission()
            .setReadPermission(true)
            .setMovePermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setDeletePermission(true);

        String sas = dataLakeFileSystemAsyncClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions)
                .setEncryptionScope(ENCRYPTION_SCOPE_STRING).setAgentObjectId(OWNER), key);

        DataLakeDirectoryAsyncClient client = getDirectoryAsyncClient(sas, dataLakeFileSystemClient.getFileSystemUrl(),
            generatePathName());
        client.create().block();

        StepVerifier.create(client.getProperties())
            .assertNext(r -> assertEquals(ENCRYPTION_SCOPE_STRING, r.getEncryptionScope()))
            .verifyComplete();
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createOptionsWithAcl() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        dc.createWithResponse(new DataLakePathCreateOptions().setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES)).block();

        StepVerifier.create(dc.getAccessControl())
            .assertNext(r -> {
                assertEquals(PATH_ACCESS_CONTROL_ENTRIES.get(0), r.getAccessControlList().get(0)); // testing if owner is set the same
                assertEquals(PATH_ACCESS_CONTROL_ENTRIES.get(1), r.getAccessControlList().get(1)); // testing if group is set the same
            })
            .verifyComplete();
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createOptionsWithOwnerAndGroup() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        dc.createWithResponse(new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName)).block();

        StepVerifier.create(dc.getAccessControl())
            .assertNext(r ->{
                assertEquals(ownerName, r.getOwner());
                assertEquals(groupName, r.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createOptionsWithNullOwnerAndGroup() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.createWithResponse(null).block();

        StepVerifier.create(dc.getAccessControl())
            .assertNext(r ->{
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
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        dc.createWithResponse(new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders)).block();

        StepVerifier.create(dc.getPropertiesWithResponse(null))
            .assertNext(r -> validatePathProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                contentMD5, contentType))
            .verifyComplete();
    }

    @ParameterizedTest //todo: replace with assert
    @CsvSource(value = {"null,null,null,null,201", "foo,bar,fizz,buzz,201"}, nullValues = "null")
    public void createOptionsWithMetadata(String key1, String value1, String key2, String value2, int statusCode) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        StepVerifier.create(dc.createWithResponse(new DataLakePathCreateOptions().setMetadata(metadata)))
            .assertNext(r -> assertEquals(statusCode, r.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(dc.getProperties())
            .assertNext(r ->{
                for (String k : metadata.keySet()) {
                    assertTrue(r.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), r.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createOptionsWithPermissionsAndUmask() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.createWithResponse(new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057")).block();

        StepVerifier.create(dc.getAccessControlWithResponse(true, null))
            .assertNext(r -> assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), r.getValue().getPermissions().toString()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createOptionsWithErrorSupplier")
    public void createOptionsWithError(String leaseId, Integer leaseDuration,
                                       DataLakePathScheduleDeletionOptions deletionOptions) {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions)
            .setProposedLeaseId(leaseId)
            .setLeaseDuration(leaseDuration);

        // assert not supported for directory
        StepVerifier.create(dc.createWithResponse(options))
            .verifyError(IllegalArgumentException.class);
    }

    private static Stream<Arguments> createOptionsWithErrorSupplier() {
        return Stream.of(
            // leaseId, leaseDuration, deletionOptions
            Arguments.of(CoreUtils.randomUuid().toString(), null, null),
            Arguments.of(CoreUtils.randomUuid().toString(), 15, null),
            Arguments.of(CoreUtils.randomUuid().toString(), null, new DataLakePathScheduleDeletionOptions(OffsetDateTime.now())),
            Arguments.of(CoreUtils.randomUuid().toString(), null, new DataLakePathScheduleDeletionOptions(Duration.ofDays(6)))
        );
    }

    @Test
    public void createIfNotExistsMin() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        assertDoesNotThrow(() -> dc.createIfNotExists().block());
    }

    @Test
    public void createIfNotExistsDefaults() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(dc.createIfNotExistsWithResponse(null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsOverwrite() { //todo: replace
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(dc.createIfNotExistsWithResponse(null))
            .assertNext(r -> assertEquals(201, r.getStatusCode()))
            .verifyComplete();

        // Try to create the resource again
        StepVerifier.create(dc.createIfNotExistsWithResponse(null))
            .assertNext(r -> assertEquals(409, r.getStatusCode()))
            .verifyComplete();

    }

    @Test
    public void createIfNotExistsDoesExist() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.createIfNotExists().block();

        StepVerifier.create(dc.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    public void createIfNotExistsHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                         String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        dc.createIfNotExistsWithResponse(new DataLakePathCreateOptions().setPathHttpHeaders(headers)).block();

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(dc.getPropertiesWithResponse(null))
            .assertNext(r -> validatePathProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage, null,
                finalContentType))
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

        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.createIfNotExistsWithResponse(new DataLakePathCreateOptions().setMetadata(metadata)).block();

        StepVerifier.create(dc.getProperties())
            .assertNext(r -> {
                for (String k : metadata.keySet()) {
                    assertEquals(metadata.get(k), r.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsPermissionsAndUmask() { //todo: replace
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");

        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName())
            .createIfNotExistsWithResponse(options))
                .assertNext(r -> assertEquals(201, r.getStatusCode()))
                .verifyComplete();
    }

    @Test
    public void deleteMin() { //replace
        StepVerifier.create(dc.deleteWithResponse(false, null))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void deleteRecursivelyAsync() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.create().block();
        // upload 5 files to the directory
        for (int i = 0; i < 5; i++) {
            dc.createFile(generatePathName()).block();
        }
        StepVerifier.create(dc.deleteRecursively()).verifyComplete();
        StepVerifier.create(dc.exists()).expectNext(false).verifyComplete();
    }

    @Test
    public void deleteRecursivelyAsyncWithResponse() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.create().block();
        // upload 5 files to the directory
        for (int i = 0; i < 5; i++) {
            dc.createFile(generatePathName()).block();
        }
        StepVerifier.create(dc.deleteRecursivelyWithResponse(null))
            .assertNext(r -> assertEquals(200, r.getStatusCode())).verifyComplete();
        StepVerifier.create(dc.exists()).expectNext(false).verifyComplete();
    }

    @Test
    public void deleteRecursive() { //todo:replace
        StepVerifier.create(dc.deleteWithResponse(true, null))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void deleteDirDoesNotExistAnymore() { //todo: fix
        /*dc.deleteWithResponse(false, null).block();

        StepVerifier.create(dc.getPropertiesWithResponse(null))
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(404, e.getStatusCode());
                assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
            });*/
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier") //todo: replace
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.deleteWithResponse(false, drc))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        setupPathLeaseCondition(dc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.deleteWithResponse(false, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExists() {
        StepVerifier.create(dc.deleteIfExists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsMin() { //todo: replace
        StepVerifier.create(dc.deleteIfExistsWithResponse(null))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsRecursive() {//todo:replace
        StepVerifier.create(dc.deleteIfExistsWithResponse(new DataLakePathDeleteOptions().setIsRecursive(true)))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsDirDoesNotExistAnymore() { //todo: replace
        StepVerifier.create(dc.deleteIfExistsWithResponse(null))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(dc.getPropertiesWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExistsDirThatWasAlreadyDeleted() { //todo: replace
        StepVerifier.create(dc.deleteIfExistsWithResponse(null))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(dc.deleteIfExistsWithResponse(null))
            .assertNext(r -> assertEquals(404, r.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier") //todo: replace
    public void deleteIfExistsAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc)
            .setIsRecursive(false);

        StepVerifier.create(dc.deleteIfExistsWithResponse(options))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        setupPathLeaseCondition(dc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc)
            .setIsRecursive(false);

        StepVerifier.create(dc.deleteIfExistsWithResponse(options))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setPermissionsMin() {
        StepVerifier.create(dc.setPermissions(PERMISSIONS, GROUP, OWNER))
            .assertNext(r -> {
                assertNotNull(r.getETag());
                assertNotNull(r.getLastModified());
            })
            .verifyComplete();
    }




    private static boolean olderThan20200210ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2020_02_10);
    }

    private static boolean olderThan20201206ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2020_12_06);
    }

    private static boolean olderThan20200612ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2020_06_12);
    }

    private static boolean olderThan20210608ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2021_06_08);
    }

    private static boolean olderThan20230803ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2023_08_03);
    }

}
