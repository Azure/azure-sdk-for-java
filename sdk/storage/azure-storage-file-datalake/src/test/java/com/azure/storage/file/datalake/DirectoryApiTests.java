// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
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
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.models.AccessControlChangeCounters;
import com.azure.storage.file.datalake.models.AccessControlChangeFailure;
import com.azure.storage.file.datalake.models.AccessControlChangeResult;
import com.azure.storage.file.datalake.models.AccessControlChanges;
import com.azure.storage.file.datalake.models.AccessControlType;
import com.azure.storage.file.datalake.models.AccessTier;
import com.azure.storage.file.datalake.models.DataLakeAclChangeFailedException;
import com.azure.storage.file.datalake.models.DataLakeAudience;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathItem;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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

public class DirectoryApiTests extends DataLakeTestBase {
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

    private DataLakeDirectoryClient dc;

    @BeforeEach
    public void setup() {
        String directoryName = generatePathName();
        dc = dataLakeFileSystemClient.getDirectoryClient(directoryName);
        dc.create();
    }

    @Test
    public void createMin() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        assertDoesNotThrow(() -> dc.create());
    }

    @Test
    public void slashTest() {
        dc = dataLakeFileSystemClient.getDirectoryClient("");
        dc.getFileClient("sample").create();
        //assertDoesNotThrow(() -> dc.create());
    }

    @Test
    public void createDefaults() {
        Response<?> createResponse = dataLakeFileSystemClient.getDirectoryClient(generatePathName())
            .createWithResponse(null, null, null, null, null, null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2021-06-08")
    @Test
    public void createDefaultsWithOptions() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setMetadata(Collections.singletonMap("foo", "bar"))
            .setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES);

        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        Response<?> createResponse = dc.createWithResponse(options, null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
        compareACL(PATH_ACCESS_CONTROL_ENTRIES, dc.getAccessControl().getAccessControlList());
    }

    @Test
    public void createDefaultsWithNullOptions() {
        Response<?> createResponse = dataLakeFileSystemClient.getDirectoryClient(generatePathName())
            .createWithResponse(null, null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
    }

    @Test
    public void createError() {
        assertThrows(DataLakeStorageException.class, () ->
            dataLakeFileSystemClient.getDirectoryClient(generatePathName())
                .createWithResponse(null, null, null, null, new DataLakeRequestConditions().setIfMatch("garbage"), null,
                    Context.NONE));
    }

    @Test
    public void createOverwrite() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.create();

        // Try to create the resource again
        assertThrows(DataLakeStorageException.class, () -> dc.create(false));
    }

    @Test
    public void exists() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.create();

        assertTrue(dc.exists());
    }

    @Test
    public void doesNotExist() {
        assertFalse(dataLakeFileSystemClient.getDirectoryClient(generatePathName()).exists());
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
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.createWithResponse(null, null, headers, null, null, null, null);
        Response<PathProperties> response = dc.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null,
            contentType);
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

        dc.createWithResponse(null, null, null, metadata, null, null, Context.NONE);
        PathProperties response = dc.getProperties();

        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(response.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), response.getMetadata().get(k));
        }
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

        assertEquals(201, dc.createWithResponse(null, null, null, null, drc, null, null).getStatusCode());
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

        assertThrows(DataLakeStorageException.class,
            () -> dc.createWithResponse(null, null, null, null, drc, null, Context.NONE));
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
        assertEquals(201, dc.createWithResponse("0777", "0057", null, null, null, null, Context.NONE).getStatusCode());
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

        PathProperties properties = client.createDirectory(generatePathName())
            .getProperties();

        assertEquals(ENCRYPTION_SCOPE_STRING, properties.getEncryptionScope());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createEncryptionScopeSas() {
        PathSasPermission permissions = new PathSasPermission()
            .setReadPermission(true)
            .setMovePermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setDeletePermission(true);

        String sas = dataLakeFileSystemClient.generateSas(new DataLakeServiceSasSignatureValues(
            testResourceNamer.now().plusDays(1), permissions).setEncryptionScope(ENCRYPTION_SCOPE_STRING));

        DataLakeDirectoryClient client = getDirectoryClient(sas, dataLakeFileSystemClient.getFileSystemUrl(),
            generatePathName());
        client.create();

        assertEquals(ENCRYPTION_SCOPE_STRING, client.getProperties().getEncryptionScope());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
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

        DataLakeDirectoryClient client = getDirectoryClient(sas, dataLakeFileSystemClient.getFileSystemUrl(),
            generatePathName());
        client.create();

        assertEquals(ENCRYPTION_SCOPE_STRING, client.getProperties().getEncryptionScope());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
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

        String sas = dataLakeFileSystemClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions)
                .setEncryptionScope(ENCRYPTION_SCOPE_STRING).setAgentObjectId(OWNER), key);

        DataLakeDirectoryClient client = getDirectoryClient(sas, dataLakeFileSystemClient.getFileSystemUrl(),
            generatePathName());
        client.create();

        assertEquals(ENCRYPTION_SCOPE_STRING, client.getProperties().getEncryptionScope());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2021-06-08")
    @Test
    public void createOptionsWithAcl() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.createWithResponse(new DataLakePathCreateOptions().setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES), null,
            null);

        List<PathAccessControlEntry> acl = dc.getAccessControl().getAccessControlList();
        assertEquals(PATH_ACCESS_CONTROL_ENTRIES.get(0), acl.get(0)); // testing if owner is set the same
        assertEquals(PATH_ACCESS_CONTROL_ENTRIES.get(1), acl.get(1)); // testing if group is set the same
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2021-06-08")
    @Test
    public void createOptionsWithOwnerAndGroup() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        dc.createWithResponse(new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName), null, null);

        assertEquals(ownerName, dc.getAccessControl().getOwner());
        assertEquals(groupName, dc.getAccessControl().getGroup());
    }

    @Test
    public void createOptionsWithNullOwnerAndGroup() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.createWithResponse(null, null, null);

        assertEquals("$superuser", dc.getAccessControl().getOwner());
        assertEquals("$superuser", dc.getAccessControl().getGroup());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null,application/octet-stream", "control,disposition,encoding,language,null,type"},
               nullValues = "null")
    public void createOptionsWithPathHttpHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, byte[] contentMD5, String contentType) {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        dc.createWithResponse(new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders), null, null);

        validatePathProperties(dc.getPropertiesWithResponse(null, null, null), cacheControl, contentDisposition,
            contentEncoding, contentLanguage, contentMD5, contentType);
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

        assertEquals(statusCode,
            dc.createWithResponse(new DataLakePathCreateOptions().setMetadata(metadata), null, null).getStatusCode());
        PathProperties properties = dc.getProperties();
        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(properties.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), properties.getMetadata().get(k));
        }
    }

    @Test
    public void createOptionsWithPermissionsAndUmask() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.createWithResponse(new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057"), null, null);

        PathAccessControl acl = dc.getAccessControlWithResponse(true, null, null, null).getValue();

        assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getPermissions().toString());
    }

    @ParameterizedTest
    @MethodSource("createOptionsWithErrorSupplier")
    public void createOptionsWithError(String leaseId, Integer leaseDuration,
        DataLakePathScheduleDeletionOptions deletionOptions) {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions)
            .setProposedLeaseId(leaseId)
            .setLeaseDuration(leaseDuration);

        // assert not supported for directory
        assertThrows(IllegalArgumentException.class, () -> dc.createWithResponse(options, null, null));
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
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        assertDoesNotThrow(() -> dc.createIfNotExists());
    }

    @Test
    public void createIfNotExistsDefaults() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());

        Response<?> createResponse = dc.createIfNotExistsWithResponse(null, null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
    }

    @Test
    public void createIfNotExistsOverwrite() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());

        assertEquals(201, dc.createIfNotExistsWithResponse(null, null, null).getStatusCode());

        // Try to create the resource again
        assertEquals(409, dc.createIfNotExistsWithResponse(null, null, null).getStatusCode());
    }

    @Test
    public void createIfNotExistsDoesExist() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.createIfNotExists();

        assertTrue(dc.exists());
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
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());

        dc.createIfNotExistsWithResponse(new DataLakePathCreateOptions().setPathHttpHeaders(headers), null, null);
        Response<PathProperties> response = dc.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null,
            contentType);
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

        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.createIfNotExistsWithResponse(new DataLakePathCreateOptions().setMetadata(metadata), null, Context.NONE);
        PathProperties response = dc.getProperties();

        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertEquals(metadata.get(k), response.getMetadata().get(k));
        }
    }

    @Test
    public void createIfNotExistsPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");

        assertEquals(201, dataLakeFileSystemClient.getDirectoryClient(generatePathName())
            .createIfNotExistsWithResponse(options, null, Context.NONE).getStatusCode());
    }

    @Test
    public void deleteMin() {
        assertEquals(200, dc.deleteWithResponse(false, null, null, null).getStatusCode());
    }

    @Test
    public void deleteRecursively() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.create();
        // upload 5 files to the directory
        for (int i = 0; i < 5; i++) {
            dc.createFile(generatePathName());
        }
        dc.deleteRecursively();
        assertFalse(dc.exists());
    }

    @Test
    public void deleteRecursivelyWithResponse() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        dc.create();
        // upload 5 files to the directory
        for (int i = 0; i < 5; i++) {
            dc.createFile(generatePathName());
        }
        Response<Void> response = dc.deleteRecursivelyWithResponse(null, null, null);
        assertEquals(200, response.getStatusCode());
        assertFalse(dc.exists());
    }

    @Test
    public void deleteRecursive() {
        assertEquals(200, dc.deleteWithResponse(true, null, null, null).getStatusCode());
    }

    @Test
    public void deleteDirDoesNotExistAnymore() {
        dc.deleteWithResponse(false, null, null, null);

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class,
            () -> dc.getPropertiesWithResponse(null, null, null));
        assertEquals(404, e.getStatusCode());
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
    }

    @Disabled("Requires manual OAuth setup and creates 5000+ files")
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2023-08-03")
    @Test
    public void deletePaginatedDirectory() {
        String entityId = "68bff720-253b-428c-b124-603700654ea9";
        DataLakeFileSystemClient fsClient = getFileSystemClientBuilder(dataLakeFileSystemClient.getFileSystemUrl())
            .credential(ENVIRONMENT.getDataLakeAccount().getCredential())
            .clientOptions(new HttpClientOptions().setProxyOptions(new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress("localhost", 8888))))
            .buildClient();

        DataLakeDirectoryClient directoryClient = fsClient.getDirectoryClient(generatePathName());
        directoryClient.create();

        for (int i = 0; i < 5020; i++) {
            DataLakeFileClient fileClient = directoryClient.getFileClient(generatePathName());
            fileClient.createIfNotExists();
        }
        DataLakeDirectoryClient rootDirectory = fsClient.getDirectoryClient("/");
        PathAccessControl acl = rootDirectory.getAccessControl();
        acl.getAccessControlList().add(new PathAccessControlEntry()
            .setPermissions(new RolePermissions()
                .setReadPermission(true)
                .setWritePermission(true)
                .setExecutePermission(true))
            .setAccessControlType(AccessControlType.USER)
            .setEntityId(entityId));

        rootDirectory.setAccessControlRecursive(acl.getAccessControlList());

        DataLakeServiceClient oAuthServiceClient = getOAuthServiceClient();
        DataLakeFileSystemClient oAuthFileSystemClient = oAuthServiceClient
            .getFileSystemClient(fsClient.getFileSystemName());
        DataLakeDirectoryClient oAuthDirectoryClient = oAuthFileSystemClient
            .getDirectoryClient(directoryClient.getDirectoryPath());

        Response<Void> response = oAuthDirectoryClient.deleteWithResponse(true, null, null, Context.NONE);
        assertEquals(response.getStatusCode(), 200);

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

        assertEquals(200, dc.deleteWithResponse(false, drc, null, null).getStatusCode());
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

        assertThrows(DataLakeStorageException.class, () -> dc.deleteWithResponse(false, drc, null, null));
    }

    @Test
    public void deleteIfExists() {
        assertTrue(dc.deleteIfExists());
    }

    @Test
    public void deleteIfExistsMin() {
        assertEquals(200, dc.deleteIfExistsWithResponse(null, null, null).getStatusCode());
    }

    @Test
    public void deleteIfExistsRecursive() {
        assertEquals(200, dc.deleteIfExistsWithResponse(new DataLakePathDeleteOptions().setIsRecursive(true), null,
                null)
            .getStatusCode());
    }

    @Test
    public void deleteIfExistsDirDoesNotExistAnymore() {
        Response<?> response = dc.deleteIfExistsWithResponse(null, null, null);
        assertEquals(200, response.getStatusCode());

        assertThrows(DataLakeStorageException.class, () -> dc.getPropertiesWithResponse(null, null, null));
    }

    @Test
    public void deleteIfExistsDirThatWasAlreadyDeleted() {
        assertEquals(200, dc.deleteIfExistsWithResponse(null, null, null).getStatusCode());
        assertEquals(404, dc.deleteIfExistsWithResponse(null, null, null).getStatusCode());
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

        assertEquals(200, dc.deleteIfExistsWithResponse(options, null, null).getStatusCode());
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

        assertThrows(DataLakeStorageException.class, () -> dc.deleteIfExistsWithResponse(options, null, null));
    }

    @Test
    public void setPermissionsMin() {
        PathInfo resp = dc.setPermissions(PERMISSIONS, GROUP, OWNER);

        assertNotNull(resp.getETag());
        assertNotNull(resp.getLastModified());
    }

    @Test
    public void setPermissionsWithResponse() {
        assertEquals(200, dc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, null, null, Context.NONE)
            .getStatusCode());
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

        assertEquals(200, dc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, drc, null, Context.NONE)
            .getStatusCode());
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

        assertThrows(DataLakeStorageException.class,
            () -> dc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, drc, null, Context.NONE));
    }

    @Test
    public void setPermissionsError() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());

        assertThrows(DataLakeStorageException.class,
            () -> dc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, null, null, null));
    }

    @Test
    public void setACLMin() {
        PathInfo resp = dc.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER);

        assertNotNull(resp.getETag());
        assertNotNull(resp.getLastModified());
    }

    @Test
    public void setACLWithResponse() {
        assertEquals(200, dc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, null, null,
            Context.NONE).getStatusCode());
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

        assertEquals(200, dc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, drc, null,
            Context.NONE).getStatusCode());
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

        assertThrows(DataLakeStorageException.class, () ->
            dc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, drc, null, Context.NONE));
    }

    @Test
    public void setACLError() {
        dc = dataLakeFileSystemClient.getDirectoryClient(generatePathName());

        assertThrows(DataLakeStorageException.class,
            () -> dc.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursiveMin() {
        setupStandardRecursiveAclTest();
        AccessControlChangeResult result = dc.setAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES);

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
        assertNull(result.getBatchFailures());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursiveBatches() {
        setupStandardRecursiveAclTest();
        PathSetAccessControlRecursiveOptions options =
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2);
        AccessControlChangeResult result = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
        assertNull(result.getBatchFailures());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursiveBatchesResume() {
        setupStandardRecursiveAclTest();
        PathSetAccessControlRecursiveOptions options =
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2).setMaxBatches(1);

        AccessControlChangeResult result = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue();

        options.setMaxBatches(null).setContinuationToken(result.getContinuationToken());
        AccessControlChangeResult result2 = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue();


        assertEquals(3L, result.getCounters().getChangedDirectoriesCount() + result2.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount() + result2.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount() + result2.getCounters().getFailedChangesCount());
        assertNull(result2.getContinuationToken());
        assertNull(result.getBatchFailures());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursiveBatchesProgress() {
        setupStandardRecursiveAclTest();
        InMemoryAccessControlRecursiveChangeProgress progress = new InMemoryAccessControlRecursiveChangeProgress();
        PathSetAccessControlRecursiveOptions options = new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setProgressHandler(progress);

        AccessControlChangeResult result = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
        assertNull(result.getBatchFailures());
        assertEquals(4, progress.batchCounters.size());
        assertEquals(2, progress.batchCounters.get(0).getChangedFilesCount() + progress.batchCounters.get(0).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(1).getChangedFilesCount() + progress.batchCounters.get(1).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(2).getChangedFilesCount() + progress.batchCounters.get(2).getChangedDirectoriesCount());
        assertEquals(1, progress.batchCounters.get(3).getChangedFilesCount() + progress.batchCounters.get(3).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.size());
        assertEquals(2, progress.cumulativeCounters.get(0).getChangedFilesCount() + progress.cumulativeCounters.get(0).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.get(1).getChangedFilesCount() + progress.cumulativeCounters.get(1).getChangedDirectoriesCount());
        assertEquals(6, progress.cumulativeCounters.get(2).getChangedFilesCount() + progress.cumulativeCounters.get(2).getChangedDirectoriesCount());
        assertEquals(7, progress.cumulativeCounters.get(3).getChangedFilesCount() + progress.cumulativeCounters.get(3).getChangedDirectoriesCount());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
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
            AccessControlChangeResult result = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue();
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

    private DataLakeDirectoryClient getSasDirectoryClient(DataLakeDirectoryClient directoryClient, String owner) {
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(null, testResourceNamer.now().plusHours(1));
        key.setSignedObjectId(testResourceNamer.recordValueFromConfig(key.getSignedObjectId()));
        key.setSignedTenantId(testResourceNamer.recordValueFromConfig(key.getSignedTenantId()));
        String sas = directoryClient.generateUserDelegationSas(new DataLakeServiceSasSignatureValues(
            testResourceNamer.now().plusHours(1), PathSasPermission.parse("racwdlmeop")).setAgentObjectId(owner), key);
        return getDirectoryClient(sas, directoryClient.getDirectoryUrl(), directoryClient.getDirectoryPath());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursiveProgressWithFailure() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient =
            getOAuthServiceClient().getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and its subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create file4 without assigning subowner permissions
        DataLakeFileClient file4 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        InMemoryAccessControlRecursiveChangeProgress progress = new InMemoryAccessControlRecursiveChangeProgress();

        AccessControlChangeResult result = subOwnerDirClient.setAccessControlRecursiveWithResponse(
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setProgressHandler(progress), null, null)
            .getValue();

        assertEquals(1, result.getCounters().getFailedChangesCount());
        assertEquals(1, progress.failures.size());
        assertTrue(progress.batchCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.cumulativeCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.failures.get(0).getName().contains(file4.getObjectName()));
        assertFalse(progress.failures.get(0).isDirectory());
        assertNotNull(progress.failures.get(0).getErrorMessage());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursiveContinueOnFailure() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create resources as super user (using shared key)
        DataLakeFileClient file4 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        DataLakeFileClient file5 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        DataLakeFileClient file6 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        DataLakeDirectoryClient subdir3 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName());

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        AccessControlChangeResult result = subOwnerDirClient.setAccessControlRecursiveWithResponse(
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
                .setContinueOnFailure(true), null, null).getValue();

        List<String> batchFailures = result.getBatchFailures().stream()
            .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(3L, result.getCounters().getChangedFilesCount());
        assertEquals(4L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());

        assertEquals(4, batchFailures.size());
        assertTrue(batchFailures.contains(file4.getObjectPath()));
        assertTrue(batchFailures.contains(file5.getObjectPath()));
        assertTrue(batchFailures.contains(file6.getObjectPath()));
        assertTrue(batchFailures.contains(subdir3.getObjectPath()));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursiveContinueOnFailureBatchFailures() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create resources as super user (using shared key)
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName());

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);
        InMemoryAccessControlRecursiveChangeProgress progress = new InMemoryAccessControlRecursiveChangeProgress();

        AccessControlChangeResult result = subOwnerDirClient.setAccessControlRecursiveWithResponse(
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setContinueOnFailure(true)
                .setBatchSize(2).setProgressHandler(progress), null, null).getValue();

        List<String> batchFailures = result.getBatchFailures().stream()
            .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(3L, result.getCounters().getChangedFilesCount());
        assertEquals(4L, result.getCounters().getFailedChangesCount());
        assertEquals(batchFailures.size(), progress.firstFailures.size());
        for (AccessControlChangeFailure f : progress.firstFailures) {
            assertTrue(batchFailures.contains(f.getName()));
        }
        assertNull(result.getContinuationToken());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursiveContinueOnFailureBatchesResume() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create resources as super user (using shared key)
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName());

        // Create more files as app
        DataLakeFileClient file7 = subdir1.createFile(generatePathName());
        DataLakeFileClient file8 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir4 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file9 = subdir4.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        file7.setPermissions(pathPermissions, null, subowner);
        file8.setPermissions(pathPermissions, null, subowner);
        subdir4.setPermissions(pathPermissions, null, subowner);
        file9.setPermissions(pathPermissions, null, subowner);

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        PathSetAccessControlRecursiveOptions options =
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setContinueOnFailure(true).setMaxBatches(1);

        AccessControlChangeResult intermediateResult = subOwnerDirClient
            .setAccessControlRecursiveWithResponse(options, null, null)
            .getValue();

        assertNotNull(intermediateResult.getContinuationToken());

        options.setMaxBatches(null).setContinuationToken(intermediateResult.getContinuationToken());
        AccessControlChangeResult result = subOwnerDirClient.setAccessControlRecursiveWithResponse(options, null, null)
            .getValue();

        assertEquals(4, result.getCounters().getChangedDirectoriesCount() + intermediateResult.getCounters().getChangedDirectoriesCount());
        assertEquals(6, result.getCounters().getChangedFilesCount() + intermediateResult.getCounters().getChangedFilesCount());
        assertEquals(4, result.getCounters().getFailedChangesCount() + intermediateResult.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
    }

    @Test
    public void setACLRecursiveError() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);

        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .getDirectoryClient(generatePathName());

        DataLakeAclChangeFailedException e = assertThrows(DataLakeAclChangeFailedException.class,
            () -> topDirOauthClient.setAccessControlRecursiveWithResponse(
                new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES), null, null));
        assertInstanceOf(DataLakeStorageException.class, e.getCause());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @ParameterizedTest
    @MethodSource("setACLRecursiveErrorSupplier")
    public void setACLRecursiveErrorMiddleOfBatches(Throwable error) {
        setupStandardRecursiveAclTest();
        PathSetAccessControlRecursiveOptions options =
            new PathSetAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2);

        // Mock a policy that will return an error on the call with the continuation token
        HttpPipelinePolicy mockPolicy = (context, next) ->
            context.getHttpRequest().getUrl().toString().contains("continuation") ? Mono.error(error) : next.process();

        dc = getDirectoryClient(getDataLakeCredential(), dc.getDirectoryUrl(), dc.getObjectPath(), mockPolicy);

        DataLakeAclChangeFailedException e = assertThrows(DataLakeAclChangeFailedException.class,
            () -> dc.setAccessControlRecursiveWithResponse(options, null, null).getValue());
        assertEquals(error.getClass(), e.getCause().getClass());

    }

    private static Stream<Throwable> setACLRecursiveErrorSupplier() {
        return Stream.of(new IllegalArgumentException(),
            new DataLakeStorageException("error",
                getStubResponse(500, new HttpRequest(HttpMethod.PUT, "https://www.fake.com")), null));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursive() {
        setupStandardRecursiveAclTest();
        AccessControlChangeResult result = dc.updateAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES);

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount());
        assertNull(result.getBatchFailures());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursiveBatches() {
        setupStandardRecursiveAclTest();
        PathUpdateAccessControlRecursiveOptions options =
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2);

        AccessControlChangeResult result = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
        assertNull(result.getBatchFailures());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursiveBatchesResume() {
        setupStandardRecursiveAclTest();
        PathUpdateAccessControlRecursiveOptions options =
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2).setMaxBatches(1);

        AccessControlChangeResult result = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue();

        options.setMaxBatches(null).setContinuationToken(result.getContinuationToken());
        AccessControlChangeResult result2 = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount() + result2.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount() + result2.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount() + result2.getCounters().getFailedChangesCount());
        assertNull(result2.getContinuationToken());
        assertNull(result.getBatchFailures());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursiveBatchesProgress() {
        setupStandardRecursiveAclTest();
        InMemoryAccessControlRecursiveChangeProgress progress = new InMemoryAccessControlRecursiveChangeProgress();
        PathUpdateAccessControlRecursiveOptions options = new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setProgressHandler(progress);

        AccessControlChangeResult result = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
        assertNull(result.getBatchFailures());
        assertEquals(4, progress.batchCounters.size());
        assertEquals(2, progress.batchCounters.get(0).getChangedFilesCount() + progress.batchCounters.get(0).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(1).getChangedFilesCount() + progress.batchCounters.get(1).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(2).getChangedFilesCount() + progress.batchCounters.get(2).getChangedDirectoriesCount());
        assertEquals(1, progress.batchCounters.get(3).getChangedFilesCount() + progress.batchCounters.get(3).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.size());
        assertEquals(2, progress.cumulativeCounters.get(0).getChangedFilesCount() + progress.cumulativeCounters.get(0).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.get(1).getChangedFilesCount() + progress.cumulativeCounters.get(1).getChangedDirectoriesCount());
        assertEquals(6, progress.cumulativeCounters.get(2).getChangedFilesCount() + progress.cumulativeCounters.get(2).getChangedDirectoriesCount());
        assertEquals(7, progress.cumulativeCounters.get(3).getChangedFilesCount() + progress.cumulativeCounters.get(3).getChangedDirectoriesCount());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
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
            AccessControlChangeResult result = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue();
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

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursiveProgressWithFailure() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create file4 as super user (using shared key)
        DataLakeFileClient file4 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        InMemoryAccessControlRecursiveChangeProgress progress = new InMemoryAccessControlRecursiveChangeProgress();

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        AccessControlChangeResult result = subOwnerDirClient.updateAccessControlRecursiveWithResponse(
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setProgressHandler(progress),
            null, null).getValue();

        assertEquals(1, result.getCounters().getFailedChangesCount());
        assertEquals(1, progress.failures.size());
        assertTrue(progress.batchCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.cumulativeCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.failures.get(0).getName().contains(file4.getObjectName()));
        assertFalse(progress.failures.get(0).isDirectory());
        assertNotNull(progress.failures.get(0).getErrorMessage());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursiveContinueOnFailure() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create resources as super user (using shared key)
        DataLakeFileClient file4 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        DataLakeFileClient file5 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        DataLakeFileClient file6 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        DataLakeDirectoryClient subdir3 = dataLakeFileSystemClient.getDirectoryClient(topDirName)
            .getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName());

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        AccessControlChangeResult result = subOwnerDirClient.updateAccessControlRecursiveWithResponse(
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
                .setContinueOnFailure(true), null, null)
            .getValue();

        List<String> batchFailures = result.getBatchFailures().stream()
            .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(3L, result.getCounters().getChangedFilesCount());
        assertEquals(4L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
        assertEquals(4, batchFailures.size());
        assertTrue(batchFailures.contains(file4.getObjectPath()));
        assertTrue(batchFailures.contains(file5.getObjectPath()));
        assertTrue(batchFailures.contains(file6.getObjectPath()));
        assertTrue(batchFailures.contains(subdir3.getObjectPath()));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursiveContinueOnFailureBatchFailures() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create resources as super user (using shared key)
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName());

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);
        InMemoryAccessControlRecursiveChangeProgress progress = new InMemoryAccessControlRecursiveChangeProgress();

        AccessControlChangeResult result = subOwnerDirClient.updateAccessControlRecursiveWithResponse(
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setContinueOnFailure(true)
                .setBatchSize(2).setProgressHandler(progress), null, null)
            .getValue();

        List<String> batchFailures = result.getBatchFailures().stream()
            .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(3L, result.getCounters().getChangedFilesCount());
        assertEquals(4L, result.getCounters().getFailedChangesCount());
        assertEquals(batchFailures.size(), progress.firstFailures.size());
        for (AccessControlChangeFailure f : progress.firstFailures) {
            assertTrue(batchFailures.contains(f.getName()));
        }
        assertNull(result.getContinuationToken());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursiveContinueOnFailureBatchesResume() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create resources as super user (using shared key)
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName());

        // Create more files as app
        DataLakeFileClient file7 = subdir1.createFile(generatePathName());
        DataLakeFileClient file8 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir4 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file9 = subdir4.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        file7.setPermissions(pathPermissions, null, subowner);
        file8.setPermissions(pathPermissions, null, subowner);
        subdir4.setPermissions(pathPermissions, null, subowner);
        file9.setPermissions(pathPermissions, null, subowner);

        PathUpdateAccessControlRecursiveOptions options = new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setContinueOnFailure(true).setMaxBatches(1);

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        AccessControlChangeResult intermediateResult = subOwnerDirClient.updateAccessControlRecursiveWithResponse(options, null, null)
            .getValue();

        assertNotNull(intermediateResult.getContinuationToken());

        options.setMaxBatches(null).setContinuationToken(intermediateResult.getContinuationToken());
        AccessControlChangeResult result = subOwnerDirClient.updateAccessControlRecursiveWithResponse(options, null, null)
            .getValue();

        assertEquals(4, result.getCounters().getChangedDirectoriesCount() + intermediateResult.getCounters().getChangedDirectoriesCount());
        assertEquals(6, result.getCounters().getChangedFilesCount() + intermediateResult.getCounters().getChangedFilesCount());
        assertEquals(4, result.getCounters().getFailedChangesCount() + intermediateResult.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
    }

    @Test
    public void updateACLRecursiveError() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .getDirectoryClient(generatePathName());

        DataLakeAclChangeFailedException e = assertThrows(DataLakeAclChangeFailedException.class, () ->
            topDirOauthClient.updateAccessControlRecursiveWithResponse(
                new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES), null, null));
        assertInstanceOf(DataLakeStorageException.class, e.getCause());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @ParameterizedTest
    @MethodSource("setACLRecursiveErrorSupplier")
    public void updateACLRecursiveErrorMiddleOfBatches(Throwable error) {
        setupStandardRecursiveAclTest();
        PathUpdateAccessControlRecursiveOptions options =
            new PathUpdateAccessControlRecursiveOptions(PATH_ACCESS_CONTROL_ENTRIES).setBatchSize(2);

        // Mock a policy that will return an error on the call with the continuation token
        HttpPipelinePolicy mockPolicy = (context, next) ->
            context.getHttpRequest().getUrl().toString().contains("continuation") ? Mono.error(error) : next.process();
        dc = getDirectoryClient(getDataLakeCredential(), dc.getDirectoryUrl(), dc.getObjectPath(), mockPolicy);

        DataLakeAclChangeFailedException e = assertThrows(DataLakeAclChangeFailedException.class,
            () -> dc.updateAccessControlRecursiveWithResponse(options, null, null));
        assertEquals(error.getClass(), e.getCause().getClass());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursive() {
        setupStandardRecursiveAclTest();
        AccessControlChangeResult result = dc.removeAccessControlRecursive(REMOVE_ACCESS_CONTROL_ENTRIES);

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursiveBatches() {
        setupStandardRecursiveAclTest();
        PathRemoveAccessControlRecursiveOptions options =
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES).setBatchSize(2);

        AccessControlChangeResult result = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
        assertNull(result.getBatchFailures());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursiveBatchesResume() {
        setupStandardRecursiveAclTest();
        PathRemoveAccessControlRecursiveOptions options = new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setMaxBatches(1);

        AccessControlChangeResult result = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue();

        options.setMaxBatches(null).setContinuationToken(result.getContinuationToken());
        AccessControlChangeResult result2 = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount() + result2.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount() + result2.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount() + result2.getCounters().getFailedChangesCount());
        assertNull(result2.getContinuationToken());
        assertNull(result.getBatchFailures());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursiveBatchesProgress() {
        setupStandardRecursiveAclTest();
        InMemoryAccessControlRecursiveChangeProgress progress = new InMemoryAccessControlRecursiveChangeProgress();
        PathRemoveAccessControlRecursiveOptions options = new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES)
            .setBatchSize(2).setProgressHandler(progress);

        AccessControlChangeResult result = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(4L, result.getCounters().getChangedFilesCount());
        assertEquals(0L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
        assertNull(result.getBatchFailures());
        assertEquals(4, progress.batchCounters.size());
        assertEquals(2, progress.batchCounters.get(0).getChangedFilesCount() + progress.batchCounters.get(0).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(1).getChangedFilesCount() + progress.batchCounters.get(1).getChangedDirectoriesCount());
        assertEquals(2, progress.batchCounters.get(2).getChangedFilesCount() + progress.batchCounters.get(2).getChangedDirectoriesCount());
        assertEquals(1, progress.batchCounters.get(3).getChangedFilesCount() + progress.batchCounters.get(3).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.size());
        assertEquals(2, progress.cumulativeCounters.get(0).getChangedFilesCount() + progress.cumulativeCounters.get(0).getChangedDirectoriesCount());
        assertEquals(4, progress.cumulativeCounters.get(1).getChangedFilesCount() + progress.cumulativeCounters.get(1).getChangedDirectoriesCount());
        assertEquals(6, progress.cumulativeCounters.get(2).getChangedFilesCount() + progress.cumulativeCounters.get(2).getChangedDirectoriesCount());
        assertEquals(7, progress.cumulativeCounters.get(3).getChangedFilesCount() + progress.cumulativeCounters.get(3).getChangedDirectoriesCount());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
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
            AccessControlChangeResult result = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue();
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

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursiveProgressWithFailure() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create file4 as super user (using shared key)
        DataLakeFileClient file4 = dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);
        InMemoryAccessControlRecursiveChangeProgress progress = new InMemoryAccessControlRecursiveChangeProgress();

        AccessControlChangeResult result = subOwnerDirClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES).setProgressHandler(progress), null, null)
            .getValue();

        assertEquals(1, result.getCounters().getFailedChangesCount());
        assertEquals(1, progress.failures.size());
        assertTrue(progress.batchCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.cumulativeCounters.stream().anyMatch(counter -> counter.getFailedChangesCount() > 0));
        assertTrue(progress.failures.get(0).getName().contains(file4.getObjectName()));
        assertFalse(progress.failures.get(0).isDirectory());
        assertNotNull(progress.failures.get(0).getErrorMessage());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursiveContinueOnFailure() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create resources as superuser (using shared key)
        DataLakeFileClient file4 = dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        DataLakeFileClient file5 = dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        DataLakeFileClient file6 = dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        DataLakeDirectoryClient subdir3 = dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName());

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        AccessControlChangeResult result = subOwnerDirClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES).setContinueOnFailure(true), null, null)
            .getValue();

        List<String> batchFailures = result.getBatchFailures().stream()
            .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(3L, result.getCounters().getChangedFilesCount());
        assertEquals(4L, result.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
        assertEquals(4, batchFailures.size());
        assertTrue(batchFailures.contains(file4.getObjectPath()));
        assertTrue(batchFailures.contains(file5.getObjectPath()));
        assertTrue(batchFailures.contains(file6.getObjectPath()));
        assertTrue(batchFailures.contains(subdir3.getObjectPath()));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursiveContinueOnFailureBatchFailures() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create resources as super user (using shared key)
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName());

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);
        InMemoryAccessControlRecursiveChangeProgress progress = new InMemoryAccessControlRecursiveChangeProgress();


        AccessControlChangeResult result = subOwnerDirClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES)
                .setContinueOnFailure(true).setBatchSize(2)
                .setProgressHandler(progress), null, null).getValue();

        List<String> batchFailures = result.getBatchFailures().stream()
            .map(AccessControlChangeFailure::getName).collect(Collectors.toList());

        assertEquals(3L, result.getCounters().getChangedDirectoriesCount()); // Including the top level
        assertEquals(3L, result.getCounters().getChangedFilesCount());
        assertEquals(4L, result.getCounters().getFailedChangesCount());
        assertEquals(batchFailures.size(), progress.firstFailures.size());
        for (AccessControlChangeFailure f : progress.firstFailures) {
            assertTrue(batchFailures.contains(f.getName()));
        }
        assertNull(result.getContinuationToken());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursiveContinueOnFailureBatchesResume() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();

        // Create tree using AAD creds
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .createDirectory(topDirName);
        DataLakeDirectoryClient subdir1 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file1 = subdir1.createFile(generatePathName());
        DataLakeFileClient file2 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file3 = subdir2.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        String subowner = testResourceNamer.randomUuid();
        RolePermissions rp = RolePermissions.parseSymbolic("rwx", false);
        PathPermissions pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp);
        topDirOauthClient.setPermissions(pathPermissions, null, subowner);
        subdir1.setPermissions(pathPermissions, null, subowner);
        file1.setPermissions(pathPermissions, null, subowner);
        file2.setPermissions(pathPermissions, null, subowner);
        subdir2.setPermissions(pathPermissions, null, subowner);
        file3.setPermissions(pathPermissions, null, subowner);

        // Create resources as super user (using shared key)
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName());
        dataLakeFileSystemClient.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName());

        // Create more files as app
        DataLakeFileClient file7 = subdir1.createFile(generatePathName());
        DataLakeFileClient file8 = subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir4 = topDirOauthClient.createSubdirectory(generatePathName());
        DataLakeFileClient file9 = subdir4.createFile(generatePathName());

        // Only allow subowner rights to the directory and it's subpaths
        file7.setPermissions(pathPermissions, null, subowner);
        file8.setPermissions(pathPermissions, null, subowner);
        subdir4.setPermissions(pathPermissions, null, subowner);
        file9.setPermissions(pathPermissions, null, subowner);

        // Create a user delegation sas that delegates an owner when creating files
        DataLakeDirectoryClient subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner);

        PathRemoveAccessControlRecursiveOptions options =
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES)
                .setBatchSize(2)
                .setContinueOnFailure(true)
                .setMaxBatches(1);

        AccessControlChangeResult intermediateResult =
            subOwnerDirClient.removeAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertNotNull(intermediateResult.getContinuationToken());

        options.setMaxBatches(null).setContinuationToken(intermediateResult.getContinuationToken());
        AccessControlChangeResult result =
            subOwnerDirClient.removeAccessControlRecursiveWithResponse(options, null, null).getValue();

        assertEquals(4, result.getCounters().getChangedDirectoriesCount() + intermediateResult.getCounters().getChangedDirectoriesCount());
        assertEquals(6, result.getCounters().getChangedFilesCount() + intermediateResult.getCounters().getChangedFilesCount());
        assertEquals(4, result.getCounters().getFailedChangesCount() + intermediateResult.getCounters().getFailedChangesCount());
        assertNull(result.getContinuationToken());
    }

    @Test
    public void removeACLRecursiveError() {
        dataLakeFileSystemClient.getRootDirectoryClient()
            .setAccessControlList(EXECUTE_ONLY_ACCESS_CONTROL_ENTRIES, null, null);
        String topDirName = generatePathName();
        DataLakeDirectoryClient topDirOauthClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
            .getDirectoryClient(topDirName);

        DataLakeAclChangeFailedException e = assertThrows(DataLakeAclChangeFailedException.class,
            () -> topDirOauthClient.removeAccessControlRecursiveWithResponse(
                new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES), null, null));
        assertInstanceOf(DataLakeStorageException.class, e.getCause());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @ParameterizedTest
    @MethodSource("setACLRecursiveErrorSupplier")
    public void removeACLRecursiveErrorMiddleOfBatches(Throwable error) {
        setupStandardRecursiveAclTest();
        PathRemoveAccessControlRecursiveOptions options =
            new PathRemoveAccessControlRecursiveOptions(REMOVE_ACCESS_CONTROL_ENTRIES).setBatchSize(2);

        // Mock a policy that will return an error on the call with the continuation token
        HttpPipelinePolicy mockPolicy = (context, next) ->
            context.getHttpRequest().getUrl().toString().contains("continuation") ? Mono.error(error) : next.process();
        dc = getDirectoryClient(getDataLakeCredential(), dc.getDirectoryUrl(), dc.getObjectPath(), mockPolicy);

        DataLakeAclChangeFailedException e = assertThrows(DataLakeAclChangeFailedException.class,
            () -> dc.removeAccessControlRecursiveWithResponse(options, null, null));
        assertEquals(error.getClass(), e.getCause().getClass());
    }

    private void setupStandardRecursiveAclTest() {
        DataLakeDirectoryClient subdir1 = dc.createSubdirectory(generatePathName());
        subdir1.createFile(generatePathName());
        subdir1.createFile(generatePathName());
        DataLakeDirectoryClient subdir2 = dc.createSubdirectory(generatePathName());
        subdir2.createFile(generatePathName());
        dc.createFile(generatePathName());
    }

    static class InMemoryAccessControlRecursiveChangeProgress implements Consumer<Response<AccessControlChanges>> {
        List<AccessControlChangeFailure> failures = new ArrayList<>();
        List<AccessControlChangeCounters> batchCounters = new ArrayList<>();
        List<AccessControlChangeCounters> cumulativeCounters = new ArrayList<>();
        List<AccessControlChangeFailure> firstFailures = new ArrayList<>();
        boolean firstFailure = false;

        @Override
        public void accept(Response<AccessControlChanges> response) {
            if (!firstFailure && response.getValue().getBatchFailures().size() > 0) {
                firstFailures.addAll(response.getValue().getBatchFailures());
                firstFailure = true;
            }
            failures.addAll(response.getValue().getBatchFailures());
            batchCounters.add(response.getValue().getBatchCounters());
            cumulativeCounters.add(response.getValue().getAggregateCounters());
        }
    }

    // set recursive acl error, with response
    // Test null or empty lists
    @Test
    public void getAccessControlMin() {
        PathAccessControl pac = dc.getAccessControl();

        assertNotNull(pac.getAccessControlList());
        assertNotNull(pac.getPermissions());
        assertNotNull(pac.getOwner());
        assertNotNull(pac.getGroup());
    }

    @Test
    public void getAccessControlWithResponse() {
        assertEquals(200, dc.getAccessControlWithResponse(false, null, null, null).getStatusCode());
    }

    @Test
    public void getAccessControlReturnUpn() {
        assertEquals(200, dc.getAccessControlWithResponse(true, null, null, null).getStatusCode());
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

        assertEquals(200, dc.getAccessControlWithResponse(false, drc, null, null).getStatusCode());
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

        assertThrows(DataLakeStorageException.class, () -> dc.getAccessControlWithResponse(false, drc, null, null));
    }

    @Test
    public void renameMin() {
        assertEquals(201, dc.renameWithResponse(null, generatePathName(), null, null, null, null).getStatusCode());
    }

    @Test
    public void renameWithResponse() {
        Response<DataLakeDirectoryClient> resp = dc.renameWithResponse(null, generatePathName(), null, null, null, null);
        DataLakeDirectoryClient renamedClient = resp.getValue();

        assertDoesNotThrow(() -> renamedClient.getProperties());
        assertThrows(DataLakeStorageException.class, () -> dc.getProperties());
    }

    @Test
    public void renameFilesystemWithResponse() {
        DataLakeFileSystemClient newFileSystem = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName());
        Response<DataLakeDirectoryClient> resp = dc.renameWithResponse(newFileSystem.getFileSystemName(),
            generatePathName(), null, null, null, null);

        DataLakeDirectoryClient renamedClient = resp.getValue();

        assertDoesNotThrow(() -> renamedClient.getProperties());
        assertThrows(DataLakeStorageException.class, () -> dc.getProperties());
    }

    @Test
    public void renameError() {
        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.getDirectoryClient(generatePathName())
            .renameWithResponse(null, generatePathName(), null, null, null, null));
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

        assertEquals(201, dc.renameWithResponse(null, generatePathName(), drc, null, null, null).getStatusCode());
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

        assertThrows(DataLakeStorageException.class,
            () -> dc.renameWithResponse(null, generatePathName(), drc, null, null, null));
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void renameDestAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient destDir = dataLakeFileSystemClient.createDirectory(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(destDir, leaseID))
            .setIfMatch(setupPathMatchCondition(destDir, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(201, dc.renameWithResponse(null, pathName, null, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void renameDestACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient destDir = dataLakeFileSystemClient.createDirectory(pathName);
        setupPathLeaseCondition(destDir, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(destDir, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> dc.renameWithResponse(null, pathName, null, drc, null, null));
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
        String sas = dataLakeFileSystemClient.generateSas(new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions));
        DataLakeDirectoryClient client = getDirectoryClient(sas, dataLakeFileSystemClient.getFileSystemUrl(), dc.getDirectoryPath());

        DataLakeDirectoryClient destClient = client.rename(dataLakeFileSystemClient.getFileSystemName(), generatePathName());

        assertNotNull(destClient.getProperties());
    }

    @Test
    public void getPropertiesDefault() {
        Response<PathProperties> response = dc.getPropertiesWithResponse(null, null, null);
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
        assertNotNull(properties.getMetadata());
        assertNull(properties.getAccessTierChangeTime());
        assertNull(properties.getEncryptionKeySha256());
        assertTrue(properties.isDirectory());
    }

    @Test
    public void getPropertiesMin() {
        assertEquals(200, dc.getPropertiesWithResponse(null, null, null).getStatusCode());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-06-12")
    @Test
    public void getPropertiesOwnerGroupPermissions() {
        PathProperties properties = dc.getPropertiesWithResponse(null, null, null).getValue();

        assertNotNull(properties.getOwner());
        assertNotNull(properties.getGroup());
        assertNotNull(properties.getPermissions());
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

        assertEquals(200, dc.getPropertiesWithResponse(drc, null, null).getStatusCode());
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

        assertThrows(DataLakeStorageException.class, () -> dc.getPropertiesWithResponse(drc, null, null));
    }

    @Test
    public void getPropertiesError() {
        DataLakeStorageException ex = assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.getDirectoryClient(generatePathName()).getProperties());

        assertTrue(ex.getMessage().contains("BlobNotFound"));
    }

    @Test
    public void setHTTPHeadersNull() {
        Response<?> response = dc.setHttpHeadersWithResponse(null, null, null, null);

        assertEquals(200, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
    }

    @Test
    public void setHTTPHeadersMin() {
        PathProperties properties = dc.getProperties();
        PathHttpHeaders headers = new PathHttpHeaders()
            .setContentEncoding(properties.getContentEncoding())
            .setContentDisposition(properties.getContentDisposition())
            .setContentType("type")
            .setCacheControl(properties.getCacheControl())
            .setContentLanguage(properties.getContentLanguage());

        dc.setHttpHeaders(headers);

        assertEquals("type", dc.getProperties().getContentType());
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

        dc.setHttpHeaders(putHeaders);

        validatePathProperties(dc.getPropertiesWithResponse(null, null, null), cacheControl, contentDisposition,
            contentEncoding, contentLanguage, contentMD5, contentType);
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

        assertEquals(200, dc.setHttpHeadersWithResponse(null, drc, null, null).getStatusCode());
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

        assertThrows(DataLakeStorageException.class, () -> dc.setHttpHeadersWithResponse(null, drc, null, null));
    }

    @Test
    public void setHTTPHeadersError() {
        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.getDirectoryClient(generatePathName()).setHttpHeaders(null));
    }

    @Test
    public void setMetadataAllNull() {
        Response<?> response = dc.setMetadataWithResponse(null, null, null, null);

        // Directories have an is directory metadata param by default
        assertEquals(1, dc.getProperties().getMetadata().size());
        assertEquals(200, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");
        dc.setMetadata(metadata);

        Map<String, String> responseMetadata = dc.getProperties().getMetadata();
        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(responseMetadata.containsKey(k));
            assertEquals(metadata.get(k), responseMetadata.get(k));
        }
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

        assertEquals(statusCode, dc.setMetadataWithResponse(metadata, null, null, null).getStatusCode());

        Map<String, String> responseMetadata = dc.getProperties().getMetadata();
        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(responseMetadata.containsKey(k));
            assertEquals(metadata.get(k), responseMetadata.get(k));
        }
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

        assertEquals(200, dc.setMetadataWithResponse(null, drc, null, null).getStatusCode());
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

        assertThrows(DataLakeStorageException.class, () -> dc.setMetadataWithResponse(null, drc, null, null));
    }

    @Test
    public void setMetadataError() {
        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.getDirectoryClient(generatePathName()).setMetadata(null));
    }

    @Test
    public void createFileMin() {
        assertDoesNotThrow(() -> dc.createFile(generatePathName()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createFileOverwrite(boolean overwrite) {
        String pathName = generatePathName();
        dc.createFile(pathName);

        if (overwrite) {
            assertDoesNotThrow(() -> dc.createFile(pathName, true));
        } else {
            assertThrows(DataLakeStorageException.class, () -> dc.createFile(pathName, false));
        }
    }

    @Test
    public void createFileError() {
        assertThrows(DataLakeStorageException.class, () -> dc.createFileWithResponse(generatePathName(), null, null,
            null, null, new DataLakeRequestConditions().setIfMatch("garbage"), null, Context.NONE));
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

        Response<PathProperties> response = dc.createFileWithResponse(generatePathName(), null, null, headers, null,
            null, null, null).getValue().getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null,
            contentType);
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

        PathProperties response = dc.createFileWithResponse(generatePathName(), null, null, null, metadata, null, null,
            null).getValue().getProperties();

        assertEquals(metadata, response.getMetadata());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dc.createFile(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(201, dc.createFileWithResponse(pathName, null, null, null, null, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void createFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dc.createFile(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> dc.createFileWithResponse(pathName, null, null, null, null, drc, null, Context.NONE));
    }

    @Test
    public void createFilePermissionsAndUmask() {
        assertEquals(201, dc.createFileWithResponse(generatePathName(), "0777", "0057", null, null, null, null,
            Context.NONE).getStatusCode());
    }

    @Test
    public void createIfNotExistsFileMin() {
        assertTrue(dc.createFileIfNotExists(generatePathName()).exists());
    }

    @Test
    public void createIfNotExistsFileOverwrite() {
        String pathName = generatePathName();

        assertEquals(201, dc.createFileIfNotExistsWithResponse(pathName, null, null, null).getStatusCode());
        assertEquals(409, dc.createFileIfNotExistsWithResponse(pathName, null, null, null).getStatusCode());
    }

    @Test
    public void createIfNotExistsFileDefaults() {
        Response<?> createResponse = dc.createFileIfNotExistsWithResponse(generatePathName(), null, null, null);


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
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(headers);

        Response<PathProperties> response = dc.createFileIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getValue().getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null,
            contentType);
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

        DataLakeFileClient client = dc.createFileIfNotExistsWithResponse(generatePathName(), options, null,
            Context.NONE).getValue();
        PathProperties response = client.getProperties();

        assertTrue(client.exists());
        assertEquals(metadata, response.getMetadata());
    }

    @Test
    public void createIfNotExistsFilePermissionsAndUmask() {
        DataLakeDirectoryClient client = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");

        assertEquals(201, client.createFileIfNotExistsWithResponse(generatePathName(), options, null, Context.NONE)
            .getStatusCode());
    }

    @Test
    public void deleteFileMin() {
        String pathName = generatePathName();
        dc.createFile(pathName);
        assertEquals(200, dc.deleteFileWithResponse(pathName, null, null, null).getStatusCode());
    }

    @Test
    public void deleteFileFileDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeFileClient client = dc.createFile(pathName);
        dc.deleteFileWithResponse(pathName, null, null, null);

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class,
            () -> client.getPropertiesWithResponse(null, null, null));

        assertEquals(404, e.getResponse().getStatusCode());
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dc.createFile(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, dc.deleteFileWithResponse(pathName, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dc.createFile(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> dc.deleteFileWithResponse(pathName, drc, null, null));
    }

    @Test
    public void deleteIfExistsFile() {
        String pathName = generatePathName();
        dc.createFile(pathName);
        assertTrue(dc.deleteFileIfExists(pathName));
    }

    @Test
    public void deleteIfExistsFileMin() {
        String pathName = generatePathName();
        dc.createFile(pathName);
        assertEquals(200, dc.deleteFileIfExistsWithResponse(pathName, null, null, null).getStatusCode());
    }

    @Test
    public void deleteIfExistsFileFileDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeFileClient client = dc.createFile(pathName);
        Response<?> response = dc.deleteFileIfExistsWithResponse(pathName, null, null, null);

        assertEquals(200, response.getStatusCode());
        assertFalse(client.exists());
    }

    @Test
    public void deleteIfExistsFileFileThatWasAlreadyDeleted() {
        String pathName = generatePathName();
        DataLakeFileClient client = dc.createFile(pathName);
        Response<?> response = dc.deleteFileIfExistsWithResponse(pathName, null, null, null);
        Response<?> response2 = dc.deleteFileIfExistsWithResponse(pathName, null, null, null);

        assertEquals(200, response.getStatusCode());
        assertFalse(client.exists());
        assertEquals(404, response2.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dc.createFile(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc);

        assertEquals(200, dc.deleteFileIfExistsWithResponse(pathName, options, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dc.createFile(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> dc.deleteFileIfExistsWithResponse(pathName,
            new DataLakePathDeleteOptions().setRequestConditions(drc), null, null));
    }

    @Test
    public void createSubDirMin() {
        assertDoesNotThrow(() -> dc.createSubdirectory(generatePathName()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createSubDirOverwrite(boolean overwrite) {
        String pathName = generatePathName();
        dc.createSubdirectory(pathName);

        if (overwrite) {
            assertDoesNotThrow(() -> dc.createSubdirectory(pathName, true));
        } else {
            assertThrows(DataLakeStorageException.class, () -> dc.createSubdirectory(pathName, false));
        }
    }

    @Test
    public void createSubDirDefaults() {
        Response<?> createResponse = dc.createSubdirectoryWithResponse(generatePathName(), null, null, null, null, null,
            null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
    }

    @Test
    public void createSubDirError() {
        assertThrows(DataLakeStorageException.class, () -> dc.createSubdirectoryWithResponse(generatePathName(), null,
                null, null, null, new DataLakeRequestConditions().setIfMatch("garbage"), null, Context.NONE));
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

        Response<PathProperties> response = dc.createSubdirectoryWithResponse(generatePathName(), null, null, headers,
            null, null, null, null).getValue()
            .getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null,
            contentType);
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

        PathProperties response = dc.createSubdirectoryWithResponse(generatePathName(), null, null, null, metadata,
            null, null, null).getValue().getProperties();


        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(response.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), response.getMetadata().get(k));
        }
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createSubDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dc.createSubdirectory(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(201, dc.createSubdirectoryWithResponse(pathName, null, null, null, null, drc, null, null)
            .getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void createSubDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dc.createSubdirectory(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> dc.createSubdirectoryWithResponse(pathName, null, null, null, null, drc, null, Context.NONE));
    }

    @Test
    public void createSubDirPermissionsAndUmask() {
        assertEquals(201, dc.createSubdirectoryWithResponse(generatePathName(), "0777", "0057", null, null, null, null,
            Context.NONE).getStatusCode());
    }

    @Test
    public void createIfNotExistsSubDirMin() {
        assertTrue(dc.createSubdirectoryIfNotExists(generatePathName()).exists());
    }

    @Test
    public void createIfNotExistsSubDirOverwrite() {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dc.createSubdirectoryIfNotExists(pathName);
        DataLakeDirectoryClient secondClient = dc.createSubdirectoryIfNotExists(pathName);

        assertTrue(client.exists());
        // same client is returned since subdirectory has already been created
        assertTrue(secondClient.exists());
        assertEquals(client.getDirectoryPath(), secondClient.getDirectoryPath());
    }

    @Test
    public void createIfNotExistsSubDirThatAlreadyExists() {
        String pathName = generatePathName();

        assertEquals(201, dc.createSubdirectoryIfNotExistsWithResponse(pathName, null, null, null).getStatusCode());
        assertEquals(409, dc.createSubdirectoryIfNotExistsWithResponse(pathName, null, null, null).getStatusCode());
    }

    @Test
    public void createIfNotExistsSubDirDefaults() {
        Response<?> createResponse = dc.createSubdirectoryIfNotExistsWithResponse(generatePathName(), null, null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
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

        Response<PathProperties> response = dc.createSubdirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPathHttpHeaders(headers), null, null).getValue()
            .getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null,
            contentType);
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

        PathProperties response = dc.createSubdirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setMetadata(metadata), null, Context.NONE).getValue()
            .getProperties();

        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(response.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), response.getMetadata().get(k));
        }
    }

    @Test
    public void createIfNotExistsSubDirPermissionsAndUmask() {
        DataLakeDirectoryClient client = dataLakeFileSystemClient.getDirectoryClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions("0777")
            .setUmask("0057");

        assertEquals(201, client.createSubdirectoryIfNotExistsWithResponse(generatePathName(), options, null,
            Context.NONE).getStatusCode());
    }

    @Test
    public void deleteSubDirMin() {
        String pathName = generatePathName();
        dc.createSubdirectory(pathName);

        assertEquals(200, dc.deleteSubdirectoryWithResponse(pathName, false, null, null, null).getStatusCode());
    }

    @Test
    public void deleteSubDirRecursive() {
        String pathName = generatePathName();
        dc.createSubdirectory(pathName);

        assertEquals(200, dc.deleteSubdirectoryWithResponse(pathName, true, null, null, null).getStatusCode());
    }

    @Test
    public void deleteSubDirDirDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dc.createSubdirectory(pathName);
        dc.deleteSubdirectoryWithResponse(pathName, false, null, null, null);

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class,
            () -> client.getPropertiesWithResponse(null, null, null));
        assertEquals(404, e.getResponse().getStatusCode());
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteSubDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dc.createSubdirectory(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, dc.deleteSubdirectoryWithResponse(pathName, false, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteSubDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dc.createSubdirectory(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> dc.deleteSubdirectoryWithResponse(pathName, false, drc, null,
            null));
    }

    @Test
    public void deleteIfExistsSubDir() {
        String pathName = generatePathName();
        dc.createSubdirectoryIfNotExists(pathName);
        assertTrue(dc.deleteSubdirectoryIfExists(pathName));
    }

    @Test
    public void deleteIfExistsSubDirMin() {
        String pathName = generatePathName();
        dc.createSubdirectoryIfNotExists(pathName);
        assertEquals(200, dc.deleteSubdirectoryIfExistsWithResponse(pathName, null, null, null).getStatusCode());
    }

    @Test
    public void deleteIfExistsSubDirRecursive() {
        String pathName = generatePathName();
        dc.createSubdirectory(pathName);
        assertEquals(200, dc.deleteSubdirectoryIfExistsWithResponse(pathName, new DataLakePathDeleteOptions()
            .setIsRecursive(true), null, null).getStatusCode());
    }

    @Test
    public void deleteIfExistsSubDirDirDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dc.createSubdirectory(pathName);

        assertEquals(200, dc.deleteSubdirectoryIfExistsWithResponse(pathName, null, null, null).getStatusCode());
        assertFalse(client.exists());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsSubDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dc.createSubdirectory(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc)
            .setIsRecursive(false);

        assertEquals(200, dc.deleteSubdirectoryIfExistsWithResponse(pathName, options, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsSubDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dc.createSubdirectory(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc)
            .setIsRecursive(false);

        assertThrows(DataLakeStorageException.class,
            () -> dc.deleteSubdirectoryIfExistsWithResponse(pathName, options, null, null));
    }

    @ParameterizedTest
    @MethodSource("fileEncodingSupplier")
    public void getDirectoryNameAndBuildClient(String originalDirectoryName) {
        DataLakeDirectoryClient client = dataLakeFileSystemClient.getDirectoryClient(originalDirectoryName);

        // Note : Here I use Path because there is a test that tests the use of a /
        assertEquals(originalDirectoryName, client.getDirectoryPath());
    }

    @Test
    public void subdirectoryAndFilePaths() {
        // testing to see if the subdirectory and file paths are created correctly
        DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("topdir");
        directoryClient.createIfNotExists();
        DataLakeDirectoryClient subDir = directoryClient.createSubdirectory("subdir");
        DataLakeDirectoryClient subSubDir = subDir.createSubdirectory("subsubdir");
        assertEquals(subDir.getDirectoryPath(), "topdir/subdir");

        // ensuring the blob and dfs endpoints are the same while creating the subdirectory
        assertEquals(subSubDir.getBlockBlobClient().getBlobUrl(),
            DataLakeImplUtils.endpointToDesiredEndpoint(subSubDir.getPathUrl(), "blob", "dfs"));
        assertEquals(subSubDir.getBlockBlobClient().getBlobName(), subSubDir.getDirectoryPath());
        assertEquals("topdir/subdir/subsubdir", subSubDir.getDirectoryPath());
        DataLakeFileClient fileClient = subSubDir.createFile("file");
        assertEquals("topdir/subdir/subsubdir/file", fileClient.getFilePath());

        // ensuring the blob and dfs endpoints are the same while creating the file
        assertEquals(fileClient.getBlockBlobClient().getBlobUrl(),
            DataLakeImplUtils.endpointToDesiredEndpoint(fileClient.getPathUrl(), "blob", "dfs"));
    }

    @ParameterizedTest
    @MethodSource("fileEncodingSupplier")
    public void createDeleteSubDirectoryUrlEncoding(String originalDirectoryName) {
        String dirName = generatePathName();
        DataLakeDirectoryClient client = dataLakeFileSystemClient.getDirectoryClient(dirName);

        Response<DataLakeDirectoryClient> resp = client.createSubdirectoryWithResponse(originalDirectoryName, null,
            null, null, null, null, null, null);

        assertEquals(201, resp.getStatusCode());
        assertEquals(dirName + "/" + originalDirectoryName, resp.getValue().getDirectoryPath());

        assertEquals(200, client.deleteSubdirectoryWithResponse(originalDirectoryName, false, null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("fileEncodingSupplier")
    public void createDeleteFileUrlEncoding(String originalFileName) {
        String fileName = generatePathName();
        DataLakeDirectoryClient client = dataLakeFileSystemClient.getDirectoryClient(fileName);

        Response<DataLakeFileClient> resp = client.createFileWithResponse(originalFileName, null, null, null, null,
            null, null, null);

        assertEquals(201, resp.getStatusCode());
        assertEquals(fileName + "/" + originalFileName, resp.getValue().getFilePath());

        assertEquals(200, client.deleteFileWithResponse(originalFileName, null, null, null).getStatusCode());
    }

    private static Stream<Arguments> fileEncodingSupplier() {
        return Stream.of(
            Arguments.of("file"),
            Arguments.of("test%test"),
            Arguments.of("test%25test"),
            Arguments.of("path%2Fto%5Da%20file"),
            Arguments.of("path/to]a file"),
            Arguments.of("斑點"),
            Arguments.of("%E6%96%91%E9%BB%9E")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "dir/file"
//        "dir%2Ffile" // no longer supported
    })
    public void createFileWithPathStructure(String pathName) {
        DataLakeFileClient fileClient = dataLakeFileSystemClient.createFile(pathName);
        // Check that service created underlying directory
        DataLakeDirectoryClient dirClient = dataLakeFileSystemClient.getDirectoryClient("dir");

        assertEquals(200, dirClient.getPropertiesWithResponse(null, null, null).getStatusCode());

        // Delete file
        assertEquals(200, fileClient.deleteWithResponse(null, null, null).getStatusCode());

        // Directory should still exist
        assertEquals(200, dirClient.getPropertiesWithResponse(null, null, null).getStatusCode());
    }

    @Test
    public void builderBearerTokenValidation() {
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
        DataLakeDirectoryClient dirClient = getOAuthServiceClient().getFileSystemClient(dc.getFileSystemName())
            .getDirectoryClient(dc.getDirectoryPath());

        assertDoesNotThrow(dirClient::getAccessControl);
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @EnabledIf("environmentServiceVersion")
    @Test
    public void perCallPolicy() {
        DataLakeDirectoryClient directoryClient = getPathClientBuilder(getDataLakeCredential(), getFileSystemUrl(),
            dc.getObjectPath()).addPolicy(getPerCallVersionPolicy())
            .buildDirectoryClient();

        // blob endpoint
        Response<?> response = directoryClient.getPropertiesWithResponse(null, null, null);

        assertEquals("2019-02-02", response.getHeaders().getValue(X_MS_VERSION));

        // dfs endpoint
        response = directoryClient.getAccessControlWithResponse(false, null, null, null);

        assertEquals("2019-02-02", response.getHeaders().getValue(X_MS_VERSION));
    }

    private static boolean environmentServiceVersion() {
        return ENVIRONMENT.getServiceVersion() != null;
    }

    private void setupDirectoryForListing(DataLakeDirectoryClient client) {
        // Create 3 subdirs
        DataLakeDirectoryClient foo = client.createSubdirectory("foo");
        client.createSubdirectory("bar");
        DataLakeDirectoryClient baz = client.createSubdirectory("baz");

        // Create subdirs for foo
        foo.createSubdirectory("foo");
        foo.createSubdirectory("bar");

        // Creat subdirs for baz
        baz.createSubdirectory("foo").createSubdirectory("bar");
        baz.createSubdirectory("bar/foo");
    }

    @Test
    public void listPaths() {
        String dirName = generatePathName();
        DataLakeDirectoryClient dir = dataLakeFileSystemClient.createDirectory(dirName);
        setupDirectoryForListing(dir);

        Iterator<PathItem> response = dir.listPaths().iterator();

        assertEquals(dirName + "/bar", response.next().getName());
        assertEquals(dirName + "/baz", response.next().getName());
        assertEquals(dirName + "/foo", response.next().getName());
        assertFalse(response.hasNext());
    }

    @Test
    public void listPathsRecursive() {
        String dirName = generatePathName();
        DataLakeDirectoryClient dir = dataLakeFileSystemClient.createDirectory(dirName);
        setupDirectoryForListing(dir);

        Iterator<PathItem> response = dir.listPaths(true, false, null, null).iterator();

        assertEquals(dirName + "/bar", response.next().getName());
        assertEquals(dirName + "/baz", response.next().getName());
        assertEquals(dirName + "/baz/bar", response.next().getName());
        assertEquals(dirName + "/baz/bar/foo", response.next().getName());
        assertEquals(dirName + "/baz/foo", response.next().getName());
        assertEquals(dirName + "/baz/foo/bar", response.next().getName());
        assertEquals(dirName + "/foo", response.next().getName());
        assertEquals(dirName + "/foo/bar", response.next().getName());
        assertEquals(dirName + "/foo/foo", response.next().getName());
        assertFalse(response.hasNext());
    }

    @Test
    public void listPathsUpn() {
        String dirName = generatePathName();
        DataLakeDirectoryClient dir = dataLakeFileSystemClient.createDirectory(dirName);
        setupDirectoryForListing(dir);

        Iterator<PathItem> response = dir.listPaths(false, true, null, null).iterator();

        PathItem first = response.next();
        assertEquals(dirName + "/bar", first.getName());
        assertNotNull(first.getGroup());
        assertNotNull(first.getOwner());
        assertEquals(dirName + "/baz", response.next().getName());
        assertEquals(dirName + "/foo", response.next().getName());
        assertFalse(response.hasNext());
    }

    @SuppressWarnings("resource")
    @Test
    public void listPathsMaxResults() {
        String dirName = generatePathName();
        DataLakeDirectoryClient dir = dataLakeFileSystemClient.createDirectory(dirName);
        setupDirectoryForListing(dir);

        PagedResponse<PathItem> response = dir.listPaths(false, false, 2, null).iterableByPage().iterator().next();

        assertEquals(2, response.getValue().size());
        assertEquals(dirName + "/bar", response.getValue().get(0).getName());
        assertEquals(dirName + "/baz", response.getValue().get(1).getName());
    }

    @Test
    public void listPathsMaxResultsByPage() {
        DataLakeDirectoryClient dir = dataLakeFileSystemClient.createDirectory(generatePathName());
        setupDirectoryForListing(dir);

        for (PagedResponse<?> page : dir.listPaths(false, false, null, null).iterableByPage(2)) {
            assertTrue(page.getValue().size() <= 2);
        }
    }

    @Test
    public void listPathsError() {
        DataLakeDirectoryClient dir = dataLakeFileSystemClient.getDirectoryClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> dir.listPaths().iterator().next());
    }

    @ParameterizedTest
    @MethodSource("getFileAndSubdirectoryClientSupplier")
    public void getFileAndSubdirectoryClient(String resourcePrefix, String subResourcePrefix) {
        String dirName = generatePathName();
        String subPath = generatePathName();
        dc = dataLakeFileSystemClient.getDirectoryClient(resourcePrefix + dirName);

        DataLakeFileClient fileClient = dc.getFileClient(subResourcePrefix + subPath);
        assertEquals(resourcePrefix + dirName + "/" + subResourcePrefix + subPath, fileClient.getFilePath());

        DataLakeDirectoryClient subDirectoryClient = dc.getSubdirectoryClient(subResourcePrefix + subPath);
        assertEquals(resourcePrefix + dirName + "/" + subResourcePrefix + subPath,
            subDirectoryClient.getDirectoryPath());
    }

    private static Stream<Arguments> getFileAndSubdirectoryClientSupplier() {
        return Stream.of(
            // resourcePrefix | subResourcePrefix
            Arguments.of("", ""),
            Arguments.of("%", "%"), // Resource has special character
            Arguments.of(Utility.urlEncode("%"), Utility.urlEncode("%")) // Sub resource has special character
        );
    }

    @Test
    public void fileInRootDirectoryRename() {
        String renamedName = generatePathName();
        dc = dataLakeFileSystemClient.getRootDirectoryClient();
        // Create file in root directory and rename
        DataLakeFileClient renamedFile = dc.createFile(generatePathName()).rename(null, renamedName);

        assertEquals(renamedName, renamedFile.getObjectPath());
        assertEquals(renamedFile.getProperties().getETag(), renamedFile.setAccessControlList(
            PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER).getETag());
    }

    @Test
    public void directoryInRootDirectoryRename() {
        String renamedName = generatePathName();
        dc = dataLakeFileSystemClient.getRootDirectoryClient();
        // Create dir in root directory and rename
        DataLakeDirectoryClient renamedDir = dc.createSubdirectory(generatePathName()).rename(null, renamedName);

        assertEquals(renamedName, renamedDir.getObjectPath());
        assertEquals(renamedDir.getProperties().getETag(), renamedDir.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES,
            GROUP, OWNER).getETag());
    }

    @Test
    public void createFileSystemWithSmallTimeoutsFailForServiceClient() {
        HttpClientOptions clientOptions = new HttpClientOptions()
            .setApplicationId("client-options-id")
            .setResponseTimeout(Duration.ofNanos(1))
            .setReadTimeout(Duration.ofNanos(1))
            .setWriteTimeout(Duration.ofNanos(1))
            .setConnectTimeout(Duration.ofNanos(1));

        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .retryOptions(new RequestRetryOptions(null, 1, (Duration) null, null, null, null))
            .clientOptions(clientOptions)
            .buildClient();

        // Loop five times as this is a timing test and it may pass by accident.
        for (int i = 0; i < 5; i++) {
            try {
                serviceClient.createFileSystem(generateFileSystemName());
            } catch (RuntimeException ex) {
                // test whether failure occurs due to small timeout intervals set on the service client
                return;
            }
        }

        fail("Expected a request to time out.");
    }

    @Test
    public void defaultAudience() {
        DataLakeDirectoryClient aadDirClient = getPathClientBuilderWithTokenCredential(
            dataLakeFileSystemClient.getFileSystemUrl(), dc.getDirectoryPath())
            .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
            .audience(null) // should default to "https://storage.azure.com/"
            .buildDirectoryClient();

        assertTrue(aadDirClient.exists());
    }

    @Test
    public void storageAccountAudience() {
        DataLakeDirectoryClient aadDirClient = getPathClientBuilderWithTokenCredential(
            ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint(), dc.getDirectoryPath())
            .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience(dataLakeFileSystemClient.getAccountName()))
            .buildDirectoryClient();

        assertTrue(aadDirClient.exists());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        DataLakeDirectoryClient aadDirClient = getPathClientBuilderWithTokenCredential(
            ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint(), dc.getDirectoryPath())
            .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience("badAudience"))
            .buildDirectoryClient();

        assertTrue(aadDirClient.exists());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", dataLakeFileSystemClient.getAccountName());
        DataLakeAudience audience = DataLakeAudience.fromString(url);

        DataLakeDirectoryClient aadDirClient = getPathClientBuilderWithTokenCredential(
            ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint(), dc.getDirectoryPath())
            .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
            .audience(audience)
            .buildDirectoryClient();

        assertTrue(aadDirClient.exists());
    }

}
