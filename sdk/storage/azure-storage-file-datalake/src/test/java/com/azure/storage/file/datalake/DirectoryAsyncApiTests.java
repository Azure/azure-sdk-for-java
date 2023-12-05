// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.common.Utility;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.file.datalake.models.AccessControlChangeFailure;
import com.azure.storage.file.datalake.models.AccessControlChangeResult;
import com.azure.storage.file.datalake.models.AccessTier;
import com.azure.storage.file.datalake.models.DataLakeAclChangeFailedException;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PathRemoveAccessControlEntry;
import com.azure.storage.file.datalake.models.RolePermissions;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.options.DataLakePathScheduleDeletionOptions;
import com.azure.storage.file.datalake.options.FileSystemEncryptionScopeOptions;
import com.azure.storage.file.datalake.options.PathRemoveAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathSetAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathUpdateAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import com.azure.storage.file.datalake.sas.PathSasPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createDefaultsWithOptions() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setMetadata(Collections.singletonMap("foo", "bar"))
            .setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES);

        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(dc.createWithResponse(options))
            .assertNext(r -> {
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
            .assertNext(r -> validatePathProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                null, finalContentType))
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
            .assertNext(r -> {
                for (String k : metadata.keySet()) {
                    assertTrue(r.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), r.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.createWithResponse(null, null, null, null, drc),
            201);
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
    public void createPermissionsAndUmask() {
        assertAsyncResponseStatusCode(dc.createWithResponse("0777", "0057", null, null,
            null), 201);
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

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20201206ServiceVersion")
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

        DataLakeDirectoryAsyncClient client = getDirectoryAsyncClient(sas, dataLakeFileSystemAsyncClient.getFileSystemUrl(),
            generatePathName());
        client.create().block();

        StepVerifier.create(client.getProperties())
            .assertNext(r -> assertEquals(ENCRYPTION_SCOPE_STRING, r.getEncryptionScope()))
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20201206ServiceVersion")
    @Test
    public void createEncryptionScopeAccountSas() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setWritePermission(true);

        String sas = getServiceClientBuilder(getDataLakeCredential(), ENVIRONMENT.getDataLakeAccount()
            .getDataLakeEndpoint())
            .encryptionScope(ENCRYPTION_SCOPE_STRING)
            .buildClient()
            .generateAccountSas(new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service,
                resourceType));

        DataLakeDirectoryAsyncClient client = getDirectoryAsyncClient(sas, dataLakeFileSystemAsyncClient.getFileSystemUrl(),
            generatePathName());
        client.create().block();

        StepVerifier.create(client.getProperties())
            .assertNext(r -> assertEquals(ENCRYPTION_SCOPE_STRING, r.getEncryptionScope()))
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20201206ServiceVersion")
    @Test
    public void createEncryptionScopeIdentitySas() {
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

        DataLakeDirectoryAsyncClient client = getDirectoryAsyncClient(sas, dataLakeFileSystemAsyncClient.getFileSystemUrl(),
            generatePathName());
        client.create().block();

        StepVerifier.create(client.getProperties())
            .assertNext(r -> assertEquals(ENCRYPTION_SCOPE_STRING, r.getEncryptionScope()))
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
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

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createOptionsWithOwnerAndGroup() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        dc.createWithResponse(new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName)).block();

        StepVerifier.create(dc.getAccessControl())
            .assertNext(r -> {
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

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,201", "foo,bar,fizz,buzz,201"}, nullValues = "null")
    public void createOptionsWithMetadata(String key1, String value1, String key2, String value2, int statusCode) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        assertAsyncResponseStatusCode(dc.createWithResponse(new DataLakePathCreateOptions().setMetadata(metadata)), statusCode);

        StepVerifier.create(dc.getProperties())
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
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        dc.createWithResponse(new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057")).block();

        StepVerifier.create(dc.getAccessControlWithResponse(true, null))
            .assertNext(r -> assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(),
                r.getValue().getPermissions().toString()))
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
            Arguments.of(CoreUtils.randomUuid().toString(), null,
                new DataLakePathScheduleDeletionOptions(OffsetDateTime.now())),
            Arguments.of(CoreUtils.randomUuid().toString(), null,
                new DataLakePathScheduleDeletionOptions(Duration.ofDays(6)))
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
    public void createIfNotExistsOverwrite() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        assertAsyncResponseStatusCode(dc.createIfNotExistsWithResponse(null), 201);

        // Try to create the resource again
        assertAsyncResponseStatusCode(dc.createIfNotExistsWithResponse(null), 409);
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
            .assertNext(r -> validatePathProperties(r, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                null, finalContentType))
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
    public void createIfNotExistsPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName())
            .createIfNotExistsWithResponse(options), 201);
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
    public void deleteRecursive() {
        assertAsyncResponseStatusCode(dc.deleteWithResponse(true, null),
            200);
    }

    @Test
    public void deleteDirDoesNotExistAnymore() {
        dc.deleteWithResponse(false, null).block();

        StepVerifier.create(dc.getPropertiesWithResponse(null))
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(404, e.getStatusCode());
                assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
            });
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.deleteWithResponse(false, drc), 200);
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
    public void deleteIfExistsMin() {
        assertAsyncResponseStatusCode(dc.deleteIfExistsWithResponse(null),
            200);
    }

    @Test
    public void deleteIfExistsRecursive() {
        assertAsyncResponseStatusCode(dc.deleteIfExistsWithResponse(new DataLakePathDeleteOptions().setIsRecursive(true)),
            200);
    }

    @Test
    public void deleteIfExistsDirDoesNotExistAnymore() {
        assertAsyncResponseStatusCode(dc.deleteIfExistsWithResponse(null), 200);

        StepVerifier.create(dc.getPropertiesWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExistsDirThatWasAlreadyDeleted() {
        assertAsyncResponseStatusCode(dc.deleteIfExistsWithResponse(null), 200);

        assertAsyncResponseStatusCode(dc.deleteIfExistsWithResponse(null), 404);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
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

        assertAsyncResponseStatusCode(dc.deleteIfExistsWithResponse(options), 200);
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

    @Test
    public void setPermissionsWithResponse() {
        assertAsyncResponseStatusCode(dc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, null),
            200);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setPermissionsAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, drc), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setPermissionsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        setupPathLeaseCondition(dc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setPermissionsError() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(dc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setACLMin() {

        StepVerifier.create(dc.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER))
            .assertNext(r -> {
                assertNotNull(r.getETag());
                assertNotNull(r.getLastModified());
            })
            .verifyComplete();
    }

    @Test
    public void setACLWithResponse() {
        assertAsyncResponseStatusCode(dc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER,
            null), 200);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setAclAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                         String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, drc),
            200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setAclACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        setupPathLeaseCondition(dc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setACLError() {
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(dc.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER))
            .verifyError(DataLakeStorageException.class);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursiveMin() {
        setupStandardRecursiveAclTest();
        StepVerifier.create(dc.setAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES))
            .assertNext(r -> {
                assertEquals(3L, r.getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getCounters().getChangedFilesCount());
                assertEquals(0L, r.getCounters().getFailedChangesCount());
                assertNull(r.getContinuationToken());
                assertNull(r.getBatchFailures());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursiveBatches() {
        setupStandardRecursiveAclTest();
        PathSetAccessControlRecursiveOptions options =
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2);
        StepVerifier.create(dc.setAccessControlRecursiveWithResponse(options))
            .assertNext(r -> {
                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(0L, r.getValue().getCounters().getFailedChangesCount());
                assertNull(r.getValue().getContinuationToken());
                assertNull(r.getValue().getBatchFailures());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursiveBatchesResume() {
        setupStandardRecursiveAclTest();
        PathSetAccessControlRecursiveOptions options =
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2).setMaxBatches(1);

        AccessControlChangeResult r = dc.setAccessControlRecursiveWithResponse(options).block().getValue();
        options.setMaxBatches(null).setContinuationToken(r.getContinuationToken());

        StepVerifier.create(dc.setAccessControlRecursiveWithResponse(options))
            .assertNext(r2 -> {
                assertEquals(3L, r.getCounters().getChangedDirectoriesCount()
                    + r2.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getCounters().getChangedFilesCount()
                    + r2.getValue().getCounters().getChangedFilesCount());
                assertEquals(0L, r.getCounters().getFailedChangesCount()
                    + r2.getValue().getCounters().getFailedChangesCount());
                assertNull(r2.getValue().getContinuationToken());
                assertNull(r.getBatchFailures());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursiveBatchesProgress() {
        setupStandardRecursiveAclTest();
        DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress progress =
            new DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress();
        PathSetAccessControlRecursiveOptions options = new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setProgressHandler(progress);

        StepVerifier.create(dc.setAccessControlRecursiveWithResponse(options))
            .assertNext(r -> {
                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(0L, r.getValue().getCounters().getFailedChangesCount());
                assertNull(r.getValue().getContinuationToken());
                assertNull(r.getValue().getBatchFailures());
            })
            .verifyComplete();

        assertEquals(4, progress.batchCounters.size());
        assertEquals(2, progress.batchCounters.get(0).getChangedFilesCount()
            + progress.batchCounters.get(0).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(1).getChangedFilesCount()
            + progress.batchCounters.get(1).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(2).getChangedFilesCount()
            + progress.batchCounters.get(2).getChangedDirectoriesCount());
        assertEquals(1, progress.batchCounters.get(3).getChangedFilesCount()
            + progress.batchCounters.get(3).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.size());
        assertEquals(2, progress.cumulativeCounters.get(0).getChangedFilesCount()
            + progress.cumulativeCounters.get(0).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.get(1).getChangedFilesCount()
            + progress.cumulativeCounters.get(1).getChangedDirectoriesCount());
        assertEquals(6, progress.cumulativeCounters.get(2).getChangedFilesCount()
            + progress.cumulativeCounters.get(2).getChangedDirectoriesCount());
        assertEquals(7, progress.cumulativeCounters.get(3).getChangedFilesCount()
            + progress.cumulativeCounters.get(3).getChangedDirectoriesCount());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursiveBatchesFollowToken() {
        setupStandardRecursiveAclTest();
        PathSetAccessControlRecursiveOptions options = new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setMaxBatches(2);

        String continuation = "null";
        int failedChanges = 0;
        int directoriesChanged = 0;
        int filesChanged = 0;
        int iterations = 0;
        while (!CoreUtils.isNullOrEmpty(continuation) && iterations < 10) {
            if (iterations == 0) {
                continuation = null; // do while not supported in Groovy
            }
            options.setContinuationToken(continuation);
            AccessControlChangeResult result = dc.setAccessControlRecursiveWithResponse(options).block().getValue();
            failedChanges += result.getCounters().getFailedChangesCount();
            directoriesChanged += result.getCounters().getChangedDirectoriesCount();
            filesChanged += result.getCounters().getChangedFilesCount();
            iterations++;
            continuation = result.getContinuationToken();
        }

        assertEquals(0, failedChanges);
        assertEquals(3, directoriesChanged); // Including the top level
        assertEquals(4, filesChanged);
        assertEquals(2, iterations);
    }

    private DataLakeDirectoryAsyncClient getSasDirectoryClient(DataLakeDirectoryAsyncClient directoryClient, String owner) {
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(null, testResourceNamer.now().plusHours(1));
        key.setSignedObjectId(testResourceNamer.recordValueFromConfig(key.getSignedObjectId()));
        key.setSignedTenantId(testResourceNamer.recordValueFromConfig(key.getSignedTenantId()));
        String sas = directoryClient.generateUserDelegationSas(new DataLakeServiceSasSignatureValues(
            testResourceNamer.now().plusHours(1), PathSasPermission.parse("racwdlmeop")).setAgentObjectId(owner), key);
        return getDirectoryAsyncClient(sas, directoryClient.getDirectoryUrl(), directoryClient.getDirectoryPath());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursiveProgressWithFailure() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and its subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create file4 without assigning subowner permissions
        DataLakeFileAsyncClient file4 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress progress =
            new DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress();

        StepVerifier.create(subOwnerDirClient.setAccessControlRecursiveWithResponse(
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setProgressHandler(progress)))
            .assertNext(r -> {
                assertEquals(1, r.getValue().getCounters().getFailedChangesCount());
            })
            .verifyComplete();

        assertEquals(1, progress.failures.size());
        assertTrue(progress.batchCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.cumulativeCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.failures.get(0).getName().contains(file4.getObjectName()));
        assertFalse(progress.failures.get(0).isDirectory());
        assertNotNull(progress.failures.get(0).getErrorMessage());

    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursiveContinueOnFailure() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create resources as super user (using shared key)
        DataLakeFileAsyncClient file4 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DataLakeFileAsyncClient file5 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DataLakeFileAsyncClient file6 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir3 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName()).block();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        StepVerifier.create(subOwnerDirClient.setAccessControlRecursiveWithResponse(
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setContinueOnFailure(true)))
            .assertNext(r -> {
                List<String> batchFailures = r.getValue().getBatchFailures().stream()
                    .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(3L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(4L, r.getValue().getCounters().getFailedChangesCount());
                assertNull(r.getValue().getContinuationToken());

                assertEquals(4, batchFailures.size());
                assertTrue(batchFailures.contains(file4.getObjectPath()));
                assertTrue(batchFailures.contains(file5.getObjectPath()));
                assertTrue(batchFailures.contains(file6.getObjectPath()));
                assertTrue(batchFailures.contains(subdir3.getObjectPath()));
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursiveContinueOnFailureBatchFailures() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create resources as super user (using shared key)
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName()).block();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);
        DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress progress =
            new DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress();

        StepVerifier.create(subOwnerDirClient.setAccessControlRecursiveWithResponse(
                new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setContinueOnFailure(true)
                    .setBatchSize(2).setProgressHandler(progress)))
            .assertNext(r -> {
                List<String> batchFailures = r.getValue().getBatchFailures().stream()
                    .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(3L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(4L, r.getValue().getCounters().getFailedChangesCount());
                assertEquals(batchFailures.size(), progress.firstFailures.size());
                for (AccessControlChangeFailure f : progress.firstFailures) {
                    assertTrue(batchFailures.contains(f.getName()));
                }
                assertNull(r.getValue().getContinuationToken());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursiveContinueOnFailureBatchesResume() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create resources as super user (using shared key)
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName()).block();

        // Create more files as app
        DataLakeFileAsyncClient file7 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file8 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir4 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file9 = subdir4.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        file7.setPermissions(pathPermissions, null, subowner).block();
        file8.setPermissions(pathPermissions, null, subowner).block();
        subdir4.setPermissions(pathPermissions, null, subowner).block();
        file9.setPermissions(pathPermissions, null, subowner).block();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        PathSetAccessControlRecursiveOptions options =
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
                .setBatchSize(2).setContinueOnFailure(true).setMaxBatches(1);

        AccessControlChangeResult intermediateResult = subOwnerDirClient
            .setAccessControlRecursiveWithResponse(options).block().getValue();

        assertNotNull(intermediateResult.getContinuationToken());

        options.setMaxBatches(null).setContinuationToken(intermediateResult.getContinuationToken());
        StepVerifier.create(subOwnerDirClient.setAccessControlRecursiveWithResponse(options))
            .assertNext(r2 -> {
                assertEquals(4, r2.getValue().getCounters().getChangedDirectoriesCount()
                    + intermediateResult.getCounters().getChangedDirectoriesCount());
                assertEquals(6, r2.getValue().getCounters().getChangedFilesCount()
                    + intermediateResult.getCounters().getChangedFilesCount());
                assertEquals(4, r2.getValue().getCounters().getFailedChangesCount()
                    + intermediateResult.getCounters().getFailedChangesCount());
                assertNull(r2.getValue().getContinuationToken());
            })
            .verifyComplete();
    }

    @Test
    public void setACLRecursiveError() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();

        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(topDirOauthClient.setAccessControlRecursiveWithResponse(
                new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)))
            .verifyErrorSatisfies(r -> {
                DataLakeAclChangeFailedException e = assertInstanceOf(DataLakeAclChangeFailedException.class, r);
                assertInstanceOf(DataLakeStorageException.class, e.getCause());
            });
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @ParameterizedTest
    @MethodSource("setACLRecursiveErrorSupplier")
    public void setACLRecursiveErrorMiddleOfBatches(Throwable error) {
        setupStandardRecursiveAclTest();
        PathSetAccessControlRecursiveOptions options =
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2);

        // Mock a policy that will return an error on the call with the continuation token
        HttpPipelinePolicy mockPolicy = (context, next) ->
            context.getHttpRequest().getUrl().toString().contains("continuation") ? Mono.error(error) : next.process();

        dc = getDirectoryAsyncClient(getDataLakeCredential(), dc.getDirectoryUrl(), dc.getObjectPath(), mockPolicy);

        StepVerifier.create(dc.setAccessControlRecursiveWithResponse(options))
            .verifyErrorSatisfies(r -> {
                DataLakeAclChangeFailedException e = assertInstanceOf(DataLakeAclChangeFailedException.class, r);
                assertEquals(error.getClass(), e.getCause().getClass());
            });
    }

    private static Stream<Throwable> setACLRecursiveErrorSupplier() {
        return Stream.of(new IllegalArgumentException(),
            new DataLakeStorageException("error",
                getStubResponse(500, new HttpRequest(HttpMethod.PUT, "https://www.fake.com")), null));
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursive() {
        setupStandardRecursiveAclTest();
        StepVerifier.create(dc.updateAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES))
            .assertNext(r -> {
                assertEquals(3L, r.getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getCounters().getChangedFilesCount());
                assertEquals(0L, r.getCounters().getFailedChangesCount());
                assertNull(r.getBatchFailures());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursiveBatches() {
        setupStandardRecursiveAclTest();
        PathUpdateAccessControlRecursiveOptions options =
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2);


        StepVerifier.create(dc.updateAccessControlRecursiveWithResponse(options))
            .assertNext(r -> {
                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(0L, r.getValue().getCounters().getFailedChangesCount());
                assertNull(r.getValue().getContinuationToken());
                assertNull(r.getValue().getBatchFailures());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursiveBatchesResume() {
        setupStandardRecursiveAclTest();
        PathUpdateAccessControlRecursiveOptions options =
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2).setMaxBatches(1);

        AccessControlChangeResult r = dc.updateAccessControlRecursiveWithResponse(options).block().getValue();

        options.setMaxBatches(null).setContinuationToken(r.getContinuationToken());
        StepVerifier.create(dc.updateAccessControlRecursiveWithResponse(options))
            .assertNext(r2 -> {
                assertEquals(3L, r.getCounters().getChangedDirectoriesCount()
                    + r2.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getCounters().getChangedFilesCount()
                    + r2.getValue().getCounters().getChangedFilesCount());
                assertEquals(0L, r.getCounters().getFailedChangesCount()
                    + r2.getValue().getCounters().getFailedChangesCount());
                assertNull(r2.getValue().getContinuationToken());
                assertNull(r.getBatchFailures());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursiveBatchesProgress() {
        setupStandardRecursiveAclTest();
        DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress progress =
            new DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress();
        PathUpdateAccessControlRecursiveOptions options = new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setProgressHandler(progress);

        StepVerifier.create(dc.updateAccessControlRecursiveWithResponse(options))
            .assertNext(r -> {
                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(0L, r.getValue().getCounters().getFailedChangesCount());
                assertNull(r.getValue().getContinuationToken());
                assertNull(r.getValue().getBatchFailures());
            })
            .verifyComplete();
        assertEquals(4, progress.batchCounters.size());
        assertEquals(2, progress.batchCounters.get(0).getChangedFilesCount()
            + progress.batchCounters.get(0).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(1).getChangedFilesCount()
            + progress.batchCounters.get(1).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(2).getChangedFilesCount()
            + progress.batchCounters.get(2).getChangedDirectoriesCount());
        assertEquals(1, progress.batchCounters.get(3).getChangedFilesCount()
            + progress.batchCounters.get(3).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.size());
        assertEquals(2, progress.cumulativeCounters.get(0).getChangedFilesCount()
            + progress.cumulativeCounters.get(0).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.get(1).getChangedFilesCount()
            + progress.cumulativeCounters.get(1).getChangedDirectoriesCount());
        assertEquals(6, progress.cumulativeCounters.get(2).getChangedFilesCount()
            + progress.cumulativeCounters.get(2).getChangedDirectoriesCount());
        assertEquals(7, progress.cumulativeCounters.get(3).getChangedFilesCount()
            + progress.cumulativeCounters.get(3).getChangedDirectoriesCount());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursiveBatchesFollowToken() {
        setupStandardRecursiveAclTest();
        PathUpdateAccessControlRecursiveOptions options = new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setMaxBatches(2);

        String continuation = "null";
        int failedChanges = 0;
        int directoriesChanged = 0;
        int filesChanged = 0;
        int iterations = 0;
        while (!CoreUtils.isNullOrEmpty(continuation) && iterations < 10) {
            if (iterations == 0) {
                continuation = null; // do while not supported in Groovy
            }
            options.setContinuationToken(continuation);
            AccessControlChangeResult result = dc.updateAccessControlRecursiveWithResponse(options).block().getValue();
            failedChanges += result.getCounters().getFailedChangesCount();
            directoriesChanged += result.getCounters().getChangedDirectoriesCount();
            filesChanged += result.getCounters().getChangedFilesCount();
            iterations++;
            continuation = result.getContinuationToken();
        }

        assertEquals(0, failedChanges);
        assertEquals(3, directoriesChanged); // Including the top level
        assertEquals(4, filesChanged);
        assertEquals(2, iterations);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursiveProgressWithFailure() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create file4 as super user (using shared key)
        DataLakeFileAsyncClient file4 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress progress =
            new DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        StepVerifier.create(subOwnerDirClient.updateAccessControlRecursiveWithResponse(
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setProgressHandler(progress)))
            .assertNext(r -> {
                assertEquals(1, r.getValue().getCounters().getFailedChangesCount());
            })
            .verifyComplete();

        assertEquals(1, progress.failures.size());
        assertTrue(progress.batchCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.cumulativeCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.failures.get(0).getName().contains(file4.getObjectName()));
        assertFalse(progress.failures.get(0).isDirectory());
        assertNotNull(progress.failures.get(0).getErrorMessage());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursiveContinueOnFailure() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create resources as super user (using shared key)
        DataLakeFileAsyncClient file4 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DataLakeFileAsyncClient file5 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DataLakeFileAsyncClient file6 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir3 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName()).block();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        StepVerifier.create(subOwnerDirClient.updateAccessControlRecursiveWithResponse(
                new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
                    .setContinueOnFailure(true)))
            .assertNext(r -> {
                List<String> batchFailures = r.getValue().getBatchFailures().stream()
                    .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(3L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(4L, r.getValue().getCounters().getFailedChangesCount());
                assertNull(r.getValue().getContinuationToken());
                assertEquals(4, batchFailures.size());
                assertTrue(batchFailures.contains(file4.getObjectPath()));
                assertTrue(batchFailures.contains(file5.getObjectPath()));
                assertTrue(batchFailures.contains(file6.getObjectPath()));
                assertTrue(batchFailures.contains(subdir3.getObjectPath()));
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursiveContinueOnFailureBatchFailures() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create resources as super user (using shared key)
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName()).block();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);
        DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress progress =
            new DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress();

        StepVerifier.create(subOwnerDirClient.updateAccessControlRecursiveWithResponse(
                new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setContinueOnFailure(true)
                    .setBatchSize(2).setProgressHandler(progress)))
            .assertNext(r -> {
                List<String> batchFailures = r.getValue().getBatchFailures().stream()
                    .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(3L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(4L, r.getValue().getCounters().getFailedChangesCount());
                assertEquals(batchFailures.size(), progress.firstFailures.size());
                for (AccessControlChangeFailure f : progress.firstFailures) {
                    assertTrue(batchFailures.contains(f.getName()));
                }
                assertNull(r.getValue().getContinuationToken());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursiveContinueOnFailureBatchesResume() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create resources as super user (using shared key)
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName()).block();

        // Create more files as app
        DataLakeFileAsyncClient file7 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file8 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir4 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file9 = subdir4.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        file7.setPermissions(pathPermissions, null, subowner).block();
        file8.setPermissions(pathPermissions, null, subowner).block();
        subdir4.setPermissions(pathPermissions, null, subowner).block();
        file9.setPermissions(pathPermissions, null, subowner).block();

        PathUpdateAccessControlRecursiveOptions options = new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setContinueOnFailure(true).setMaxBatches(1);

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        AccessControlChangeResult intermediateResult = subOwnerDirClient.updateAccessControlRecursiveWithResponse(options)
            .block().getValue();

        assertNotNull(intermediateResult.getContinuationToken());

        options.setMaxBatches(null).setContinuationToken(intermediateResult.getContinuationToken());
        StepVerifier.create(subOwnerDirClient.updateAccessControlRecursiveWithResponse(options))
            .assertNext(r2 -> {
                assertEquals(4, r2.getValue().getCounters().getChangedDirectoriesCount()
                    + intermediateResult.getCounters().getChangedDirectoriesCount());
                assertEquals(6, r2.getValue().getCounters().getChangedFilesCount()
                    + intermediateResult.getCounters().getChangedFilesCount());
                assertEquals(4, r2.getValue().getCounters().getFailedChangesCount()
                    + intermediateResult.getCounters().getFailedChangesCount());
                assertNull(r2.getValue().getContinuationToken());
            })
            .verifyComplete();
    }

    @Test
    public void updateACLRecursiveError() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(topDirOauthClient.updateAccessControlRecursiveWithResponse(
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)))
            .verifyErrorSatisfies(r -> {
                DataLakeAclChangeFailedException e = assertInstanceOf(DataLakeAclChangeFailedException.class, r);
                assertInstanceOf(DataLakeStorageException.class, e.getCause());
            });
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @ParameterizedTest
    @MethodSource("setACLRecursiveErrorSupplier")
    public void updateACLRecursiveErrorMiddleOfBatches(Throwable error) {
        setupStandardRecursiveAclTest();
        PathUpdateAccessControlRecursiveOptions options =
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2);

        // Mock a policy that will return an error on the call with the continuation token
        HttpPipelinePolicy mockPolicy = (context, next) ->
            context.getHttpRequest().getUrl().toString().contains("continuation") ? Mono.error(error) : next.process();
        dc = getDirectoryAsyncClient(getDataLakeCredential(), dc.getDirectoryUrl(), dc.getObjectPath(), mockPolicy);

        StepVerifier.create(dc.updateAccessControlRecursiveWithResponse(options))
            .verifyErrorSatisfies(r -> {
                DataLakeAclChangeFailedException e = assertInstanceOf(DataLakeAclChangeFailedException.class, r);
                assertEquals(error.getClass(), e.getCause().getClass());
            });
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void removeACLRecursive() {
        setupStandardRecursiveAclTest();
        StepVerifier.create(dc.removeAccessControlRecursive(REMOVE_ACCESS_CONTROL_ENTRIES))
            .assertNext(r -> {
                assertEquals(3L, r.getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getCounters().getChangedFilesCount());
                assertEquals(0L, r.getCounters().getFailedChangesCount());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void removeACLRecursiveBatches() {
        setupStandardRecursiveAclTest();
        PathRemoveAccessControlRecursiveOptions options =
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES).setBatchSize(2);

        StepVerifier.create(dc.removeAccessControlRecursiveWithResponse(options))
            .assertNext(r -> {
                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(0L, r.getValue().getCounters().getFailedChangesCount());
                assertNull(r.getValue().getContinuationToken());
                assertNull(r.getValue().getBatchFailures());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void removeACLRecursiveBatchesResume() {
        setupStandardRecursiveAclTest();
        PathRemoveAccessControlRecursiveOptions options = new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setMaxBatches(1);

        AccessControlChangeResult r = dc.removeAccessControlRecursiveWithResponse(options).block().getValue();

        options.setMaxBatches(null).setContinuationToken(r.getContinuationToken());
        StepVerifier.create(dc.removeAccessControlRecursiveWithResponse(options))
            .assertNext(r2 -> {
                assertEquals(3L, r.getCounters().getChangedDirectoriesCount()
                    + r2.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getCounters().getChangedFilesCount()
                    + r2.getValue().getCounters().getChangedFilesCount());
                assertEquals(0L, r.getCounters().getFailedChangesCount()
                    + r2.getValue().getCounters().getFailedChangesCount());
                assertNull(r2.getValue().getContinuationToken());
                assertNull(r.getBatchFailures());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void removeACLRecursiveBatchesProgress() {
        setupStandardRecursiveAclTest();
        DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress progress =
            new DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress();
        PathRemoveAccessControlRecursiveOptions options = new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setProgressHandler(progress);

        StepVerifier.create(dc.removeAccessControlRecursiveWithResponse(options))
            .assertNext(r -> {
                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(4L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(0L, r.getValue().getCounters().getFailedChangesCount());
                assertNull(r.getValue().getContinuationToken());
                assertNull(r.getValue().getBatchFailures());
            })
            .verifyComplete();
        assertEquals(4, progress.batchCounters.size());
        assertEquals(2, progress.batchCounters.get(0).getChangedFilesCount()
            + progress.batchCounters.get(0).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(1).getChangedFilesCount()
            + progress.batchCounters.get(1).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(2).getChangedFilesCount()
            + progress.batchCounters.get(2).getChangedDirectoriesCount());
        assertEquals(1, progress.batchCounters.get(3).getChangedFilesCount()
            + progress.batchCounters.get(3).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.size());
        assertEquals(2, progress.cumulativeCounters.get(0).getChangedFilesCount()
            + progress.cumulativeCounters.get(0).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.get(1).getChangedFilesCount()
            + progress.cumulativeCounters.get(1).getChangedDirectoriesCount());
        assertEquals(6, progress.cumulativeCounters.get(2).getChangedFilesCount()
            + progress.cumulativeCounters.get(2).getChangedDirectoriesCount());
        assertEquals(7, progress.cumulativeCounters.get(3).getChangedFilesCount()
            + progress.cumulativeCounters.get(3).getChangedDirectoriesCount());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void removeACLRecursiveBatchesFollowToken() {
        setupStandardRecursiveAclTest();
        PathRemoveAccessControlRecursiveOptions options =
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES).setBatchSize(2).setMaxBatches(2);

        String continuation = "null";
        int failedChanges = 0;
        int directoriesChanged = 0;
        int filesChanged = 0;
        int iterations = 0;
        while (!CoreUtils.isNullOrEmpty(continuation) && iterations < 10) {
            if (iterations == 0) {
                continuation = null; // do while not supported in Groovy
            }
            options.setContinuationToken(continuation);
            AccessControlChangeResult result = dc.removeAccessControlRecursiveWithResponse(options).block().getValue();
            failedChanges += result.getCounters().getFailedChangesCount();
            directoriesChanged += result.getCounters().getChangedDirectoriesCount();
            filesChanged += result.getCounters().getChangedFilesCount();
            iterations++;
            continuation = result.getContinuationToken();
        }

        assertEquals(0, failedChanges);
        assertEquals(3, directoriesChanged); // Including the top level
        assertEquals(4, filesChanged);
        assertEquals(2, iterations);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void removeACLRecursiveProgressWithFailure() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and its subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create file4 without assigning subowner permissions
        DataLakeFileAsyncClient file4 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress progress =
            new DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress();

        StepVerifier.create(subOwnerDirClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES).setProgressHandler(progress)))
            .assertNext(r -> {
                assertEquals(1, r.getValue().getCounters().getFailedChangesCount());
            })
            .verifyComplete();

        assertEquals(1, progress.failures.size());
        assertTrue(progress.batchCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.cumulativeCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.failures.get(0).getName().contains(file4.getObjectName()));
        assertFalse(progress.failures.get(0).isDirectory());
        assertNotNull(progress.failures.get(0).getErrorMessage());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void removeACLRecursiveContinueOnFailure() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create resources as super user (using shared key)
        DataLakeFileAsyncClient file4 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DataLakeFileAsyncClient file5 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DataLakeFileAsyncClient file6 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir3 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName)
            .getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName()).block();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        StepVerifier.create(subOwnerDirClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES).setContinueOnFailure(true)))
            .assertNext(r -> {
                List<String> batchFailures = r.getValue().getBatchFailures().stream()
                    .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(3L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(4L, r.getValue().getCounters().getFailedChangesCount());
                assertNull(r.getValue().getContinuationToken());
                assertEquals(4, batchFailures.size());
                assertTrue(batchFailures.contains(file4.getObjectPath()));
                assertTrue(batchFailures.contains(file5.getObjectPath()));
                assertTrue(batchFailures.contains(file6.getObjectPath()));
                assertTrue(batchFailures.contains(subdir3.getObjectPath()));
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void removeACLRecursiveContinueOnFailureBatchFailures() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create resources as super user (using shared key)
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName()).block();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);
        DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress progress =
            new DirectoryApiTests.InMemoryAccessControlRecursiveChangeProgress();

        StepVerifier.create(subOwnerDirClient.removeAccessControlRecursiveWithResponse(
                new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES)
                    .setContinueOnFailure(true).setBatchSize(2)
                    .setProgressHandler(progress)))
            .assertNext(r -> {
                List<String> batchFailures = r.getValue().getBatchFailures().stream()
                    .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

                assertEquals(3L, r.getValue().getCounters().getChangedDirectoriesCount()); // Including the top level
                assertEquals(3L, r.getValue().getCounters().getChangedFilesCount());
                assertEquals(4L, r.getValue().getCounters().getFailedChangesCount());
                assertEquals(batchFailures.size(), progress.firstFailures.size());
                for (AccessControlChangeFailure f : progress.firstFailures) {
                    assertTrue(batchFailures.contains(f.getName()));
                }
                assertNull(r.getValue().getContinuationToken());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void removeACLRecursiveContinueOnFailureBatchesResume() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .createDirectory(topDirName).block();
        DataLakeDirectoryAsyncClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file1 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file2 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file3 = subdir2.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner).block();
        subdir1.setPermissions(pathPermissions, null, subowner).block();
        file1.setPermissions(pathPermissions, null, subowner).block();
        file2.setPermissions(pathPermissions, null, subowner).block();
        subdir2.setPermissions(pathPermissions, null, subowner).block();
        file3.setPermissions(pathPermissions, null, subowner).block();

        // Create resources as super user (using shared key)
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createFile(generatePathName()).block();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(topDirName).getSubdirectoryAsyncClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName()).block();

        // Create more files as app
        DataLakeFileAsyncClient file7 = subdir1.createFile(generatePathName()).block();
        DataLakeFileAsyncClient file8 = subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir4 = topDirOauthClient.createSubdirectory(generatePathName()).block();
        DataLakeFileAsyncClient file9 = subdir4.createFile(generatePathName()).block();

        // Only allow subowner rights to the directory and it's subpaths
        file7.setPermissions(pathPermissions, null, subowner).block();
        file8.setPermissions(pathPermissions, null, subowner).block();
        subdir4.setPermissions(pathPermissions, null, subowner).block();
        file9.setPermissions(pathPermissions, null, subowner).block();

        PathRemoveAccessControlRecursiveOptions options =
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES)
                .setBatchSize(2)
                .setContinueOnFailure(true)
                .setMaxBatches(1);

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryAsyncClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        AccessControlChangeResult intermediateResult = subOwnerDirClient.removeAccessControlRecursiveWithResponse(options)
            .block().getValue();

        assertNotNull(intermediateResult.getContinuationToken());

        options.setMaxBatches(null).setContinuationToken(intermediateResult.getContinuationToken());
        StepVerifier.create(subOwnerDirClient.removeAccessControlRecursiveWithResponse(options))
            .assertNext(r2 -> {
                assertEquals(4, r2.getValue().getCounters().getChangedDirectoriesCount()
                    + intermediateResult.getCounters().getChangedDirectoriesCount());
                assertEquals(6, r2.getValue().getCounters().getChangedFilesCount()
                    + intermediateResult.getCounters().getChangedFilesCount());
                assertEquals(4, r2.getValue().getCounters().getFailedChangesCount()
                    + intermediateResult.getCounters().getFailedChangesCount());
                assertNull(r2.getValue().getContinuationToken());
            })
            .verifyComplete();
    }

    @Test
    public void removeACLRecursiveError() {
        dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null).block();
        DataLakeDirectoryAsyncClient topDirOauthClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(topDirOauthClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES)))
            .verifyErrorSatisfies(r -> {
                DataLakeAclChangeFailedException e = assertInstanceOf(DataLakeAclChangeFailedException.class, r);
                assertInstanceOf(DataLakeStorageException.class, e.getCause());
            });
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @ParameterizedTest
    @MethodSource("setACLRecursiveErrorSupplier")
    public void removeACLRecursiveErrorMiddleOfBatches(Throwable error) {
        setupStandardRecursiveAclTest();
        PathRemoveAccessControlRecursiveOptions options =
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES).setBatchSize(2);

        // Mock a policy that will return an error on the call with the continuation token
        HttpPipelinePolicy mockPolicy = (context, next) ->
            context.getHttpRequest().getUrl().toString().contains("continuation") ? Mono.error(error) : next.process();
        dc = getDirectoryAsyncClient(getDataLakeCredential(), dc.getDirectoryUrl(), dc.getObjectPath(), mockPolicy);

        StepVerifier.create(dc.removeAccessControlRecursiveWithResponse(options))
            .verifyErrorSatisfies(r -> {
                DataLakeAclChangeFailedException e = assertInstanceOf(DataLakeAclChangeFailedException.class, r);
                assertEquals(error.getClass(), e.getCause().getClass());
            });
    }

    private void setupStandardRecursiveAclTest() {
        DataLakeDirectoryAsyncClient subdir1 = dc.createSubdirectory(generatePathName()).block();
        subdir1.createFile(generatePathName()).block();
        subdir1.createFile(generatePathName()).block();
        DataLakeDirectoryAsyncClient subdir2 = dc.createSubdirectory(generatePathName()).block();
        subdir2.createFile(generatePathName()).block();
        dc.createFile(generatePathName()).block();
    }

    // set recursive acl error, with response
    // Test null or empty lists
    @Test
    public void getAccessControlMin() {
        StepVerifier.create(dc.getAccessControl())
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
        assertAsyncResponseStatusCode(dc.getAccessControlWithResponse(false, null),
            200);
    }

    @Test
    public void getAccessControlReturnUpn() {
        assertAsyncResponseStatusCode(dc.getAccessControlWithResponse(true, null),
            200);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void getAccessControlAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.getAccessControlWithResponse(false, drc),
            200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void getAccessControlACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                       String noneMatch, String leaseID) {
        if (GARBAGE_LEASE_ID.equals(leaseID)) {
            return; // known issue - remove when resolved.
        }

        setupPathLeaseCondition(dc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.getAccessControlWithResponse(false, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void renameMin() {
        assertAsyncResponseStatusCode(dc.renameWithResponse(null, generatePathName(),
            null, null), 201);
    }

    @Test
    public void renameWithResponse() {
        StepVerifier.create(dc.renameWithResponse(null, generatePathName(), null,
                null))
            .assertNext(r -> {
                DataLakeDirectoryAsyncClient renamedClient = r.getValue();
                assertDoesNotThrow(renamedClient::getProperties);
            })
            .verifyComplete();

        StepVerifier.create(dc.getProperties())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void renameFilesystemWithResponse() {
        DataLakeFileSystemAsyncClient newFileSystem =
            primaryDataLakeServiceAsyncClient.createFileSystem(generateFileSystemName()).block();
        StepVerifier.create(dc.renameWithResponse(newFileSystem.getFileSystemName(),
            generatePathName(), null, null))
            .assertNext(r -> {
                DataLakeDirectoryAsyncClient renamedClient = r.getValue();
                assertDoesNotThrow(renamedClient::getProperties);
            })
            .verifyComplete();

        StepVerifier.create(dc.getProperties())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void renameError() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName())
            .renameWithResponse(null, generatePathName(), null,
                null))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void renameSourceAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.renameWithResponse(null, generatePathName(), drc,
            null), 201);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void renameSourceACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String leaseID) {
        setupPathLeaseCondition(dc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.renameWithResponse(null, generatePathName(), drc, null))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void renameDestAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient destDir = dataLakeFileSystemAsyncClient.createDirectory(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(destDir, leaseID))
            .setIfMatch(setupPathMatchCondition(destDir, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.renameWithResponse(null, pathName, null, drc),
            201);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void renameDestACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient destDir = dataLakeFileSystemAsyncClient.createDirectory(pathName).block();
        setupPathLeaseCondition(destDir, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(destDir, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.renameWithResponse(null, pathName, null, drc))
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
        String sas = dataLakeFileSystemAsyncClient.generateSas(
            new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions));
        DataLakeDirectoryAsyncClient client = getDirectoryAsyncClient(
            sas, dataLakeFileSystemAsyncClient.getFileSystemUrl(), dc.getDirectoryPath());

        StepVerifier.create(client.rename(dataLakeFileSystemAsyncClient.getFileSystemName(), generatePathName()))
            .assertNext(r -> assertNotNull(r.getProperties()))
            .verifyComplete();
    }

    @Test
    public void getPropertiesDefault() {
        StepVerifier.create(dc.getPropertiesWithResponse(null))
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
                assertNotNull(properties.getMetadata());
                assertNull(properties.getAccessTierChangeTime());
                assertNull(properties.getEncryptionKeySha256());
                assertTrue(properties.isDirectory());
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesMin() {
        assertAsyncResponseStatusCode(dc.getPropertiesWithResponse(null),
            200);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200612ServiceVersion")
    @Test
    public void getPropertiesOwnerGroupPermissions() {
        StepVerifier.create(dc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertNotNull(r.getValue().getOwner());
                assertNotNull(r.getValue().getGroup());
                assertNotNull(r.getValue().getPermissions());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void getPropertiesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.getPropertiesWithResponse(drc), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void getPropertiesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                    String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.getPropertiesWithResponse(drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getPropertiesError() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).getProperties())
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException ex = assertInstanceOf(DataLakeStorageException.class, r);
                assertTrue(ex.getMessage().contains("BlobNotFound"));
            });
    }

    @Test
    public void setHTTPHeadersNull() {
        StepVerifier.create(dc.setHttpHeadersWithResponse(null, null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void setHTTPHeadersMin() {
        PathProperties properties = dc.getProperties().block();
        PathHttpHeaders headers = new PathHttpHeaders()
            .setContentEncoding(properties.getContentEncoding())
            .setContentDisposition(properties.getContentDisposition())
            .setContentType("type")
            .setCacheControl(properties.getCacheControl())
            .setContentLanguage(properties.getContentLanguage());

        dc.setHttpHeaders(headers).block();

        assertEquals("type", dc.getProperties().block().getContentType());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null,null", "control,disposition,encoding,language,null,type"},
        nullValues = "null")
    public void setHTTPHeadersHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                      String contentLanguage, byte[] contentMD5, String contentType) {
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        dc.setHttpHeaders(putHeaders).block();
        StepVerifier.create(dc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                validatePathProperties(r, cacheControl, contentDisposition,
                    contentEncoding, contentLanguage, contentMD5, contentType);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setHttpHeadersAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.setHttpHeadersWithResponse(null, drc), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setHttpHeadersACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        setupPathLeaseCondition(dc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.setHttpHeadersWithResponse(null, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setHTTPHeadersError() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).setHttpHeaders(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setMetadataAllNull() {
        StepVerifier.create(dc.setMetadataWithResponse(null, null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();

        // Directories have an is directory metadata param by default
        assertEquals(1, dc.getProperties().block().getMetadata().size());
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");
        dc.setMetadata(metadata).block();

        StepVerifier.create(dc.getProperties())
            .assertNext(r -> {
                for (String k : metadata.keySet()) {
                    assertTrue(r.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), r.getMetadata().get(k));
                }
            })
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

        assertAsyncResponseStatusCode(dc.setMetadataWithResponse(metadata, null), statusCode);

        StepVerifier.create(dc.getProperties())
            .assertNext(r -> {
                for (String k : metadata.keySet()) {
                    assertTrue(r.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), r.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setMetadataAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                              String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.setMetadataWithResponse(null, drc), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setMetadataACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                  String leaseID) {
        setupPathLeaseCondition(dc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.setMetadataWithResponse(null, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setMetadataError() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).setMetadata(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createFileMin() {
        assertDoesNotThrow(() -> dc.createFile(generatePathName()).block());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createFileOverwrite(boolean overwrite) {
        String pathName = generatePathName();
        dc.createFile(pathName).block();

        if (overwrite) {
            assertDoesNotThrow(() -> dc.createFile(pathName, true).block());
        } else {
            StepVerifier.create(dc.createFile(pathName, false))
                .verifyError(DataLakeStorageException.class);
        }
    }

    @Test
    public void createFileError() {
        StepVerifier.create(dc.createFileWithResponse(generatePathName(), null, null,
            null, null, new DataLakeRequestConditions().setIfMatch("garbage")))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    public void createFileHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                  String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);

        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        String finalContentType = contentType;
        StepVerifier.create(dc.createFileWithResponse(generatePathName(), null, null, headers, null,
                null)
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                null, finalContentType))
            .verifyComplete();
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

        StepVerifier.create(dc.createFileWithResponse(generatePathName(), null, null, null, metadata, null)
            .flatMap(r -> r.getValue().getProperties()))
            .assertNext(p -> assertEquals(metadata, p.getMetadata()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dc.createFile(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.createFileWithResponse(pathName, null, null, null,
            null, drc), 201);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void createFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dc.createFile(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.createFileWithResponse(pathName, null, null, null, null, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createFilePermissionsAndUmask() {
        assertAsyncResponseStatusCode(dc.createFileWithResponse(generatePathName(), "0777", "0057",
            null, null, null), 201);
    }

    @Test
    public void createIfNotExistsFileMin() {
        StepVerifier.create(dc.createFileIfNotExists(generatePathName())
            .flatMap(r -> r.exists()))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsFileOverwrite() {
        String pathName = generatePathName();

        assertAsyncResponseStatusCode(dc.createFileIfNotExistsWithResponse(pathName, null), 201);

        assertAsyncResponseStatusCode(dc.createFileIfNotExistsWithResponse(pathName, null), 409);
    }

    @Test
    public void createIfNotExistsFileDefaults() {
        StepVerifier.create(dc.createFileIfNotExistsWithResponse(generatePathName(), null))
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
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(headers);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(dc.createFileIfNotExistsWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                null, finalContentType))
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
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setMetadata(metadata);

        DataLakeFileAsyncClient client = dc.createFileIfNotExistsWithResponse(generatePathName(), options).block().getValue();

        StepVerifier.create(client.exists())
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsFilePermissionsAndUmask() {
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");

        assertAsyncResponseStatusCode(client.createFileIfNotExistsWithResponse(generatePathName(), options),
            201);
    }

    @Test
    public void deleteFileMin() {
        String pathName = generatePathName();
        dc.createFile(pathName).block();

        assertAsyncResponseStatusCode(dc.deleteFileWithResponse(pathName, null), 200);
    }

    @Test
    public void deleteFileFileDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dc.createFile(pathName).block();
        dc.deleteFileWithResponse(pathName, null).block();

        StepVerifier.create(client.getPropertiesWithResponse(null))
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(404, e.getResponse().getStatusCode());
                assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
            });
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dc.createFile(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.deleteFileWithResponse(pathName, drc), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dc.createFile(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.deleteFileWithResponse(pathName, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExistsFile() {
        String pathName = generatePathName();
        dc.createFile(pathName).block();

        StepVerifier.create(dc.deleteFileIfExists(pathName))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileMin() {
        String pathName = generatePathName();
        dc.createFile(pathName).block();

        assertAsyncResponseStatusCode(dc.deleteFileIfExistsWithResponse(pathName, null), 200);
    }

    @Test
    public void deleteIfExistsFileFileDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dc.createFile(pathName).block();

        assertAsyncResponseStatusCode(dc.deleteFileIfExistsWithResponse(pathName, null), 200);

        StepVerifier.create(client.exists())
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileFileThatWasAlreadyDeleted() {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dc.createFile(pathName).block();

        assertAsyncResponseStatusCode(dc.deleteFileIfExistsWithResponse(pathName, null), 200);

        StepVerifier.create(client.exists())
            .expectNext(false)
            .verifyComplete();

        assertAsyncResponseStatusCode(dc.deleteFileIfExistsWithResponse(pathName, null), 404);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dc.createFile(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc);

        assertAsyncResponseStatusCode(dc.deleteFileIfExistsWithResponse(pathName, options), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                         String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dc.createFile(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.deleteFileIfExistsWithResponse(pathName,
            new DataLakePathDeleteOptions().setRequestConditions(drc)))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createSubDirMin() {
        assertDoesNotThrow(() -> dc.createSubdirectory(generatePathName()).block());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createSubDirOverwrite(boolean overwrite) {
        String pathName = generatePathName();
        dc.createSubdirectory(pathName).block();

        if (overwrite) {
            assertDoesNotThrow(() -> dc.createSubdirectory(pathName, true).block());
        } else {
            StepVerifier.create(dc.createSubdirectory(pathName, false))
                .verifyError(DataLakeStorageException.class);
        }
    }

    @Test
    public void createSubDirDefaults() {
        StepVerifier.create(dc.createSubdirectoryWithResponse(generatePathName(), null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createSubDirError() {
        StepVerifier.create(dc.createSubdirectoryWithResponse(generatePathName(), null,
            null, null, null, new DataLakeRequestConditions().setIfMatch("garbage")))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    public void createSubDirHeaders(String cacheControl, String contentDisposition, String contentEncoding,
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
        StepVerifier.create(dc.createSubdirectoryWithResponse(generatePathName(), null, null, headers,
            null, null)
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                null, finalContentType))
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createSubDirMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        StepVerifier.create(dc.createSubdirectoryWithResponse(generatePathName(), null, null, null,
            metadata, null)
            .flatMap(r -> r.getValue().getProperties()))
            .assertNext(p -> {
                for (String k : metadata.keySet()) {
                    assertTrue(p.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), p.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createSubDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dc.createSubdirectory(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.createSubdirectoryWithResponse(pathName, null, null, null,
            null, drc), 201);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void createSubDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dc.createSubdirectory(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.createSubdirectoryWithResponse(pathName, null, null, null,
                null, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createSubDirPermissionsAndUmask() {
        assertAsyncResponseStatusCode(dc.createSubdirectoryWithResponse(generatePathName(), "0777",
            "0057", null, null, null), 201);
    }

    @Test
    public void createIfNotExistsSubDirMin() {
        StepVerifier.create(dc.createSubdirectoryIfNotExists(generatePathName())
            .flatMap(r -> r.exists()))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirOverwrite() {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dc.createSubdirectoryIfNotExists(pathName).block();
        DataLakeDirectoryAsyncClient secondClient = dc.createSubdirectoryIfNotExists(pathName).block();

        StepVerifier.create(client.exists())
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(secondClient.exists())
            .expectNext(true)
            .verifyComplete();

        assertEquals(client.getDirectoryPath(), secondClient.getDirectoryPath());
    }

    @Test
    public void createIfNotExistsSubDirThatAlreadyExists() {
        String pathName = generatePathName();

        assertAsyncResponseStatusCode(dc.createSubdirectoryIfNotExistsWithResponse(pathName, null),
            201);
        assertAsyncResponseStatusCode(dc.createSubdirectoryIfNotExistsWithResponse(pathName, null),
            409);
    }

    @Test
    public void createIfNotExistsSubDirDefaults() {
        StepVerifier.create(dc.createSubdirectoryIfNotExistsWithResponse(generatePathName(), null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    public void createIfNotExistsSubDirHeaders(String cacheControl, String contentDisposition, String contentEncoding,
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
        StepVerifier.create(dc.createSubdirectoryIfNotExistsWithResponse(generatePathName(),
                new DataLakePathCreateOptions().setPathHttpHeaders(headers))
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                null, finalContentType))
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createIfNotExistsSubDirMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        StepVerifier.create(dc.createSubdirectoryIfNotExistsWithResponse(generatePathName(),
                new DataLakePathCreateOptions().setMetadata(metadata))
            .flatMap(r -> r.getValue().getProperties()))
            .assertNext(p -> {
                for (String k : metadata.keySet()) {
                    assertTrue(p.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), p.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsSubDirPermissionsAndUmask() {
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions("0777")
            .setUmask("0057");

        assertAsyncResponseStatusCode(client.createSubdirectoryIfNotExistsWithResponse(generatePathName(), options),
            201);
    }

    @Test
    public void deleteSubDirMin() {
        String pathName = generatePathName();
        dc.createSubdirectory(pathName).block();

        assertAsyncResponseStatusCode(dc.deleteSubdirectoryWithResponse(pathName, false, null),
            200);
    }

    @Test
    public void deleteSubDirRecursive() {
        String pathName = generatePathName();
        dc.createSubdirectory(pathName).block();

        StepVerifier.create(dc.deleteSubdirectoryWithResponse(pathName, true, null))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void deleteSubDirDirDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dc.createSubdirectory(pathName).block();
        dc.deleteSubdirectoryWithResponse(pathName, false, null).block();

        StepVerifier.create(client.getPropertiesWithResponse(null))
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(404, e.getResponse().getStatusCode());
                assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
            });
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteSubDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                               String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dc.createSubdirectory(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dc.deleteSubdirectoryWithResponse(pathName, false, drc), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteSubDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                   String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dc.createSubdirectory(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dc.deleteSubdirectoryWithResponse(pathName, false, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExistsSubDir() {
        String pathName = generatePathName();
        dc.createSubdirectoryIfNotExists(pathName).block();

        StepVerifier.create(dc.deleteSubdirectoryIfExists(pathName))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsSubDirMin() {
        String pathName = generatePathName();
        dc.createSubdirectoryIfNotExists(pathName).block();

        //cannot run without setting recursive options?
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(false);

        assertAsyncResponseStatusCode(dc.deleteSubdirectoryIfExistsWithResponse(pathName, options), 200);
    }

    @Test
    public void deleteIfExistsSubDirRecursive() {
        String pathName = generatePathName();
        dc.createSubdirectory(pathName).block();

        assertAsyncResponseStatusCode(dc.deleteSubdirectoryIfExistsWithResponse(pathName, new DataLakePathDeleteOptions()
            .setIsRecursive(true)), 200);
    }

    @Test
    public void deleteIfExistsSubDirDirDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dc.createSubdirectory(pathName).block();

        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(false);

        assertAsyncResponseStatusCode(dc.deleteSubdirectoryIfExistsWithResponse(pathName, options), 200);

        StepVerifier.create(client.exists())
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsSubDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                       String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dc.createSubdirectory(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc)
            .setIsRecursive(false);

        assertAsyncResponseStatusCode(dc.deleteSubdirectoryIfExistsWithResponse(pathName, options), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsSubDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                           String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dc.createSubdirectory(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc)
            .setIsRecursive(false);

        StepVerifier.create(dc.deleteSubdirectoryIfExistsWithResponse(pathName, options))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("fileEncodingSupplier")
    public void getDirectoryNameAndBuildClient(String originalDirectoryName, String finalDirectoryName) {
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(originalDirectoryName);

        // Note : Here I use Path because there is a test that tests the use of a /
        assertEquals(finalDirectoryName, client.getDirectoryPath());
    }

    @ParameterizedTest
    @MethodSource("fileEncodingSupplier")
    public void createDeleteSubDirectoryUrlEncoding(String originalDirectoryName, String finalDirectoryName) {
        String dirName = generatePathName();
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(dirName);

        StepVerifier.create(client.createSubdirectoryWithResponse(originalDirectoryName, null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                assertEquals(dirName + "/" + finalDirectoryName, r.getValue().getDirectoryPath());
            })
            .verifyComplete();

        assertAsyncResponseStatusCode(client.deleteSubdirectoryWithResponse(originalDirectoryName, false,
            null), 200);
    }

    @ParameterizedTest
    @MethodSource("fileEncodingSupplier")
    public void createDeleteFileUrlEncoding(String originalFileName, String finalFileName) {
        String fileName = generatePathName();
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(fileName);

        StepVerifier.create(client.createFileWithResponse(originalFileName, null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                assertEquals(fileName + "/" + finalFileName, r.getValue().getFilePath());
            })
            .verifyComplete();

        assertAsyncResponseStatusCode(client.deleteFileWithResponse(originalFileName, null),
            200);
    }

    private static Stream<Arguments> fileEncodingSupplier() {
        return Stream.of(
            // originalName | finalName
            Arguments.of("file", "file"),
            Arguments.of("path/to]a file", "path/to]a file"),
            Arguments.of("path%2Fto%5Da%20file", "path/to]a file"),
            Arguments.of("斑點", "斑點"),
            Arguments.of("%E6%96%91%E9%BB%9E", "斑點")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"dir/file", "dir%2Ffile"})
    public void createFileWithPathStructure(String pathName) {
        DataLakeFileAsyncClient fileClient = dataLakeFileSystemAsyncClient.createFile(pathName).block();
        // Check that service created underlying directory
        DataLakeDirectoryAsyncClient dirClient = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient("dir");

        StepVerifier.create(dirClient.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        // Delete file
        assertAsyncResponseStatusCode(fileClient.deleteWithResponse(null, null, null),
            200);

        // Directory should still exist
        assertAsyncResponseStatusCode(dirClient.getPropertiesWithResponse(null), 200);
    }

    @Test
    public void builderBearerTokenValidation() { //duplicate
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder
        // fails
        String endpoint = BlobUrlParts.parse(dc.getDirectoryUrl()).setScheme("http").toUrl().toString();

        assertThrows(IllegalArgumentException.class, () -> new DataLakePathClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildDirectoryClient());
    }

    @Test
    public void getAccessControlOAuth() {
        DataLakeDirectoryAsyncClient dirClient = getOAuthServiceAsyncClient().getFileSystemAsyncClient(dc.getFileSystemName())
            .getDirectoryAsyncClient(dc.getDirectoryPath());

        assertDoesNotThrow(dirClient::getAccessControl);
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @EnabledIf("environmentServiceVersion")
    @Test
    public void perCallPolicy() {
        DataLakeDirectoryAsyncClient directoryClient = getPathClientBuilder(getDataLakeCredential(), getFileSystemUrl(),
            dc.getObjectPath()).addPolicy(getPerCallVersionPolicy())
            .buildDirectoryAsyncClient();

        // blob endpoint
        StepVerifier.create(directoryClient.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals("2019-02-02", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();

        // dfs endpoint
        StepVerifier.create(directoryClient.getAccessControlWithResponse(false, null))
            .assertNext(r -> assertEquals("2019-02-02", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    private static boolean environmentServiceVersion() {
        return ENVIRONMENT.getServiceVersion() != null;
    }

    private void setupDirectoryForListing(DataLakeDirectoryAsyncClient client) {
        // Create 3 subdirs
        DataLakeDirectoryAsyncClient foo = client.createSubdirectory("foo").block();
        client.createSubdirectory("bar").block();
        DataLakeDirectoryAsyncClient baz = client.createSubdirectory("baz").block();

        // Create subdirs for foo
        foo.createSubdirectory("foo").block();
        foo.createSubdirectory("bar").block();

        // Creat subdirs for baz
        baz.createSubdirectory("foo").block().createSubdirectory("bar").block();
        baz.createSubdirectory("bar/foo").block();
    }

    @Test
    public void listPaths() {
        String dirName = generatePathName();
        DataLakeDirectoryAsyncClient dir = dataLakeFileSystemAsyncClient.createDirectory(dirName).block();
        setupDirectoryForListing(dir);

        StepVerifier.create(dir.listPaths())
            .assertNext(r -> assertEquals(dirName + "/bar", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/baz", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/foo", r.getName()))
            .verifyComplete();
    }

    @Test
    public void listPathsRecursive() {
        String dirName = generatePathName();
        DataLakeDirectoryAsyncClient dir = dataLakeFileSystemAsyncClient.createDirectory(dirName).block();
        setupDirectoryForListing(dir);

        StepVerifier.create(dir.listPaths(true, false, null))
            .assertNext(r -> assertEquals(dirName + "/bar", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/baz", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/baz/bar", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/baz/bar/foo", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/baz/foo", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/baz/foo/bar", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/foo", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/foo/bar", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/foo/foo", r.getName()))
            .verifyComplete();
    }

    @Test
    public void listPathsUpn() {
        String dirName = generatePathName();
        DataLakeDirectoryAsyncClient dir = dataLakeFileSystemAsyncClient.createDirectory(dirName).block();
        setupDirectoryForListing(dir);

        StepVerifier.create(dir.listPaths(false, true, null))
            .assertNext(r -> {
                assertEquals(dirName + "/bar", r.getName());
                assertNotNull(r.getGroup());
                assertNotNull(r.getOwner());
            })
            .assertNext(r -> assertEquals(dirName + "/baz", r.getName()))
            .assertNext(r -> assertEquals(dirName + "/foo", r.getName()))
            .verifyComplete();
    }

    @SuppressWarnings("resource")
    @Test
    public void listPathsMaxResults() {
        String dirName = generatePathName();
        DataLakeDirectoryAsyncClient dir = dataLakeFileSystemAsyncClient.createDirectory(dirName).block();
        setupDirectoryForListing(dir);

        StepVerifier.create(dir.listPaths(false, false, 2).byPage())
            .assertNext(r -> {
                assertEquals(2, r.getValue().size());
                assertEquals(dirName + "/bar", r.getValue().get(0).getName());
                assertEquals(dirName + "/baz", r.getValue().get(1).getName());
            })
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void listPathsMaxResultsByPage() {
        DataLakeDirectoryAsyncClient dir = dataLakeFileSystemAsyncClient.createDirectory(generatePathName()).block();
        setupDirectoryForListing(dir);

        StepVerifier.create(dir.listPaths(false, false, null).byPage(2))
            .assertNext(r -> assertTrue(r.getValue().size() <= 2))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void listPathsError() {
        DataLakeDirectoryAsyncClient dir = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName());

        StepVerifier.create(dir.listPaths())
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("getFileAndSubdirectoryClientSupplier")
    public void getFileAndSubdirectoryClient(String resourcePrefix, String subResourcePrefix) {
        String dirName = generatePathName();
        String subPath = generatePathName();
        dc = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(resourcePrefix + dirName);

        DataLakeFileAsyncClient fileClient = dc.getFileAsyncClient(subResourcePrefix + subPath);
        assertEquals(Utility.urlDecode(resourcePrefix) + dirName + "/" + Utility.urlDecode(subResourcePrefix) + subPath,
            fileClient.getFilePath());

        DataLakeDirectoryAsyncClient subDirectoryClient = dc.getSubdirectoryAsyncClient(subResourcePrefix + subPath);
        assertEquals(Utility.urlDecode(resourcePrefix) + dirName + "/" + Utility.urlDecode(subResourcePrefix) + subPath,
            subDirectoryClient.getDirectoryPath());
    }

    private static Stream<Arguments> getFileAndSubdirectoryClientSupplier() {
        return Stream.of(
            // resourcePrefix | subResourcePrefix
            Arguments.of("", ""),
            Arguments.of("", Utility.urlEncode("%")), // Resource has special character
            Arguments.of(Utility.urlEncode("%"), ""), // Sub resource has special character
            Arguments.of(Utility.urlEncode("%"), Utility.urlEncode("%"))
        );
    }

    @Test
    public void fileInRootDirectoryRename() {
        String renamedName = generatePathName();
        dc = dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient();
        // Create file in root directory and rename
        DataLakeFileAsyncClient renamedFile = dc.createFile(generatePathName()).block().rename(
            null, renamedName).block();

        assertEquals(renamedName, renamedFile.getObjectPath());
        assertEquals(renamedFile.getProperties().block().getETag(), renamedFile.setAccessControlList(
            PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER).block().getETag());
    }

    @Test
    public void directoryInRootDirectoryRename() {
        String renamedName = generatePathName();
        dc = dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient();
        // Create dir in root directory and rename
        DataLakeDirectoryAsyncClient renamedDir = dc.createSubdirectory(generatePathName()).block().rename(
            null, renamedName).block();

        assertEquals(renamedName, renamedDir.getObjectPath());
        assertEquals(renamedDir.getProperties().block().getETag(), renamedDir.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES,
            GROUP, OWNER).block().getETag());
    }

    @Test
    public void createFileSystemWithSmallTimeoutsFailForServiceClient() {
        HttpClientOptions clientOptions = new HttpClientOptions()
            .setApplicationId("client-options-id")
            .setResponseTimeout(Duration.ofNanos(1))
            .setReadTimeout(Duration.ofNanos(1))
            .setWriteTimeout(Duration.ofNanos(1))
            .setConnectTimeout(Duration.ofNanos(1));

        DataLakeServiceAsyncClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .retryOptions(new RequestRetryOptions(null, 1, (Duration) null, null,
                null, null))
            .clientOptions(clientOptions)
            .buildAsyncClient();

        // Loop five times as this is a timing test and it may pass by accident.
        for (int i = 0; i < 5; i++) {
            try {
                serviceClient.createFileSystem(generateFileSystemName()).block();
            } catch (RuntimeException ex) {
                // test whether failure occurs due to small timeout intervals set on the service client
                return;
            }
        }

        fail("Expected a request to time out.");
    }
}
