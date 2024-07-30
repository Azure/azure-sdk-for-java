// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.datalake.models.AccessControlType;
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemAccessPolicies;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.RolePermissions;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import com.azure.storage.file.datalake.sas.PathSasPermission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SasAsyncTests extends DataLakeTestBase {
    private DataLakeFileAsyncClient sasClient;
    private String pathName;

    @BeforeEach
    public void setup() {
        pathName = generatePathName();
        sasClient = getFileAsyncClient(getDataLakeCredential(), getFileSystemUrl(), pathName);
        sasClient.create().block();
        sasClient.append(DATA.getDefaultBinaryData(), 0).block();
        sasClient.flush(DATA.getDefaultDataSizeLong(), true).block();
    }

    private DataLakeServiceSasSignatureValues generateValues(PathSasPermission permission) {
        return new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setCacheControl("cache")
            .setContentDisposition("disposition")
            .setContentEncoding("encoding")
            .setContentLanguage("language")
            .setContentType("type");
    }

    private static void validateSasProperties(PathProperties properties) {
        assertEquals("cache", properties.getCacheControl());
        assertEquals("disposition", properties.getContentDisposition());
        assertEquals("encoding", properties.getContentEncoding());
        assertEquals("language", properties.getContentLanguage());
    }

    private UserDelegationKey getUserDelegationInfo() {
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(testResourceNamer.now().minusDays(1),
            testResourceNamer.now().plusDays(1));
        String keyOid = testResourceNamer.recordValueFromConfig(key.getSignedObjectId());
        key.setSignedObjectId(keyOid);
        String keyTid = testResourceNamer.recordValueFromConfig(key.getSignedTenantId());
        key.setSignedTenantId(keyTid);

        return key;
    }

    @Test
    public void fileSasPermission() {
        PathSasPermission permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true);

        if (Constants.SAS_SERVICE_VERSION.compareTo(DataLakeServiceVersion.V2019_12_12.getVersion()) >= 0) {
            permissions.setMovePermission(true)
                .setExecutePermission(true)
                .setManageOwnershipPermission(true)
                .setManageAccessControlPermission(true);
        }

        DataLakeFileAsyncClient client = getFileAsyncClient(sasClient.generateSas(generateValues(permissions)),
            getFileSystemUrl(), pathName);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(client.read()))
            .assertNext(r -> assertArrayEquals(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void directorySasPermission() {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient sasClient = getDirectoryAsyncClient(getDataLakeCredential(), getFileSystemUrl(),
            pathName);

        Mono<PathInfo> create = sasClient.create();

        Mono<String> sas = Mono.just(sasClient.generateSas(generateValues(new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setManageOwnershipPermission(true)
            .setManageAccessControlPermission(true))));

        Mono<PathProperties> response1 = create.then(sas).flatMap(r -> {
            DataLakeDirectoryAsyncClient client = getDirectoryAsyncClient(r, getFileSystemUrl(), pathName);
            return client.getProperties();
        });
        StepVerifier.create(response1)
            .assertNext(SasAsyncTests::validateSasProperties)
            .verifyComplete();

        Mono<DataLakeDirectoryAsyncClient> response2 = sas.flatMap(r -> {
            DataLakeDirectoryAsyncClient client = getDirectoryAsyncClient(r, getFileSystemUrl(), pathName);
            return client.createSubdirectory(generatePathName());
        });
        StepVerifier.create(response2)
            .expectNextCount(1)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void directorySasPermissionFail() {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient sasClient = getDirectoryAsyncClient(getDataLakeCredential(), getFileSystemUrl(),
            pathName);
        Mono<PathProperties> response = sasClient.create().flatMap(r -> {
            String sas = sasClient.generateSas(generateValues(new PathSasPermission() /* No read permission. */
                    .setWritePermission(true)
                    .setDeletePermission(true)
                    .setCreatePermission(true)));
            return getDirectoryAsyncClient(sas, getFileSystemUrl(), pathName).getProperties();
        });

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void fileSystemSasIdentifier() {
        //todo isbr
        DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier().setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)));
        dataLakeFileSystemAsyncClient.setAccessPolicy(null, Collections.singletonList(identifier)).block();

        // Wait 30 seconds as it may take time for the access policy to take effect.
        waitUntilPredicate(1000, 30, () -> {
            FileSystemAccessPolicies accessPolicies = dataLakeFileSystemAsyncClient.getAccessPolicy().block();

            if (accessPolicies == null || accessPolicies.getIdentifiers() == null
                || accessPolicies.getIdentifiers().size() != 1) {
                return false;
            }

            DataLakeSignedIdentifier signedIdentifier = accessPolicies.getIdentifiers().get(0);
            return signedIdentifier.getId().equals(identifier.getId())
                && signedIdentifier.getAccessPolicy().getPermissions().equals(identifier.getAccessPolicy().getPermissions())
                && signedIdentifier.getAccessPolicy().getExpiresOn().equals(identifier.getAccessPolicy().getExpiresOn());
        });

        // Check containerSASPermissions
        FileSystemSasPermission permissions = new FileSystemSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true);

        if (Constants.SAS_SERVICE_VERSION.compareTo(DataLakeServiceVersion.V2020_02_10.getVersion()) >= 0) {
            permissions.setMovePermission(true)
                .setExecutePermission(true)
                .setManageOwnershipPermission(true)
                .setManageAccessControlPermission(true);
        }

        DataLakeServiceSasSignatureValues sasValues = new DataLakeServiceSasSignatureValues(identifier.getId());
        DataLakeFileSystemAsyncClient client1 = getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.generateSas(sasValues),
            getFileSystemUrl());

        StepVerifier.create(client1.listPaths())
            .expectNextCount(1)
            .verifyComplete();

        sasValues = new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions);
        DataLakeFileSystemAsyncClient client2 = getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.generateSas(sasValues),
            getFileSystemUrl());

        StepVerifier.create(client2.listPaths())
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void fileUserDelegation() {
        PathSasPermission permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true);

        if (Constants.SAS_SERVICE_VERSION.compareTo(DataLakeServiceVersion.V2019_12_12.getVersion()) >= 0) {
            permissions.setMovePermission(true)
                .setExecutePermission(true)
                .setManageOwnershipPermission(true)
                .setManageAccessControlPermission(true);
        }

        DataLakeFileAsyncClient client = getFileAsyncClient(sasClient.generateUserDelegationSas(generateValues(permissions),
            getUserDelegationInfo()), getFileSystemUrl(), pathName);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(client.read()))
            .assertNext(r -> assertArrayEquals(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void directoryUserDelegation() {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient sasClient = getDirectoryAsyncClient(getDataLakeCredential(), getFileSystemUrl(),
            pathName);
        Mono<PathInfo> create = sasClient.create();
        PathSasPermission permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setManageOwnershipPermission(true)
            .setManageAccessControlPermission(true);

        Mono<DataLakeDirectoryAsyncClient> client = Mono.just(getDirectoryAsyncClient(sasClient.generateUserDelegationSas(generateValues(permissions), getUserDelegationInfo()), getFileSystemUrl(), pathName));

        StepVerifier.create(create.then(client).flatMap(DataLakePathAsyncClient::getProperties))
            .assertNext(SasAsyncTests::validateSasProperties)
            .verifyComplete();

        StepVerifier.create(client.flatMap(r -> r.createSubdirectory(generatePathName())))
            .expectNextCount(1)
            .verifyComplete();


        dataLakeFileSystemAsyncClient = getFileSystemAsyncClient(sasClient
            .generateUserDelegationSas(generateValues(permissions), getUserDelegationInfo()), getFileSystemUrl());

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths(new ListPathsOptions().setPath(pathName)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void fileSystemUserDelegation() {
        FileSystemSasPermission permissions = new FileSystemSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true);

        if (Constants.SAS_SERVICE_VERSION.compareTo(DataLakeServiceVersion.V2020_02_10.getVersion()) >= 0) {
            permissions.setMovePermission(true)
                .setExecutePermission(true)
                .setManageOwnershipPermission(true)
                .setManageAccessControlPermission(true);
        }

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime);
        key.setSignedObjectId(testResourceNamer.recordValueFromConfig(key.getSignedObjectId()));
        key.setSignedTenantId(testResourceNamer.recordValueFromConfig(key.getSignedTenantId()));

        String sasWithPermissions = dataLakeFileSystemAsyncClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions), key);

        DataLakeFileSystemAsyncClient client = getFileSystemAsyncClient(sasWithPermissions, getFileSystemUrl());

        StepVerifier.create(client.listPaths())
            .expectNextCount(1)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void fileUserDelegationSAOID() {
        String saoid = testResourceNamer.randomUuid();
        String pathName = generatePathName();

        PathSasPermission permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setManageOwnershipPermission(true)
            .setManageAccessControlPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime);
        key.setSignedObjectId(testResourceNamer.recordValueFromConfig(key.getSignedObjectId()));
        key.setSignedTenantId(testResourceNamer.recordValueFromConfig(key.getSignedTenantId()));

        /* Grant userOID on root folder. */
        DataLakeDirectoryAsyncClient rootClient = getDirectoryAsyncClient(getDataLakeCredential(), getFileSystemUrl(),
            "");

        Mono<PathAccessControl> step = rootClient.setAccessControlList(Collections.singletonList(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setEntityId(saoid)
            .setPermissions(RolePermissions.parseSymbolic("rwx", false))), null, null)
            .flatMap(r -> {
                String sasWithPermissions = rootClient.generateUserDelegationSas(
                    new DataLakeServiceSasSignatureValues(expiryTime, permissions).setPreauthorizedAgentObjectId(saoid),
                    key);
                return Mono.just(sasWithPermissions);
            })
            .flatMap(sas -> {
                DataLakeFileAsyncClient client = getFileAsyncClient(sas, getFileSystemUrl(), pathName);
                assertTrue(sas.contains("saoid=" + saoid));
                return client.create(true)
                    .then(client.append(DATA.getDefaultBinaryData(), 0))
                    .then(client.flush(DATA.getDefaultDataSizeLong(), true));
            })
            .flatMap(client -> {
                DataLakeFileAsyncClient newClient = getFileAsyncClient(getDataLakeCredential(), getFileSystemUrl(), pathName);
                return newClient.getAccessControl();
            });

        StepVerifier.create(step)
            .assertNext(r ->  assertEquals(saoid, r.getOwner()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void fileUserDelegationSUOID() {
        String suoid = testResourceNamer.randomUuid();
        String pathName = generatePathName();

        PathSasPermission permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setManageOwnershipPermission(true)
            .setManageAccessControlPermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime);
        key.setSignedObjectId(testResourceNamer.recordValueFromConfig(key.getSignedObjectId()));
        key.setSignedTenantId(testResourceNamer.recordValueFromConfig(key.getSignedTenantId()));

        // User is not authorized yet.
        String sasWithPermissions = sasClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions).setAgentObjectId(suoid), key);
        DataLakeFileAsyncClient client = getFileAsyncClient(sasWithPermissions, getFileSystemUrl(), pathName);

        Mono<PathInfo> response = client.create(true)
            .then(client.append(DATA.getDefaultBinaryData(), 0))
            .then(client.flush(DATA.getDefaultDataSizeLong(), true));

        StepVerifier.create(response)
            .verifyError(DataLakeStorageException.class);

        assertTrue(sasWithPermissions.contains("suoid=" + suoid));

        // User is now authorized.
        /* Grant userOID on root folder. */
        DataLakeDirectoryAsyncClient rootClient = getDirectoryAsyncClient(getDataLakeCredential(), getFileSystemUrl(),
                "");
        Mono<PathAccessControl> step = rootClient.setAccessControlList(Collections.singletonList(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setEntityId(suoid)
            .setPermissions(RolePermissions.parseSymbolic("rwx", false))), null, null)
            .flatMap(r -> {
                String newSasWithPermissions = rootClient.generateUserDelegationSas(
                    new DataLakeServiceSasSignatureValues(expiryTime, permissions).setAgentObjectId(suoid), key);
                return Mono.just(newSasWithPermissions);
            })
            .flatMap(sas -> {
                DataLakeFileAsyncClient newClient = getFileAsyncClient(sas, getFileSystemUrl(), pathName);
                assertTrue(sas.contains("suoid=" + suoid));
                return newClient.create(true)
                    .then(newClient.append(DATA.getDefaultBinaryData(), 0))
                    .then(newClient.flush(DATA.getDefaultDataSizeLong(), true));
            })
            .flatMap(c -> {
                DataLakeFileAsyncClient newClient = getFileAsyncClient(getDataLakeCredential(), getFileSystemUrl(), pathName);
                return newClient.getAccessControl();
            });

        StepVerifier.create(step)
                .assertNext(r ->  assertEquals(suoid, r.getOwner()))
                .verifyComplete();

        // Use random other suoid. User should not be authorized.
        String sasWithoutPermission = rootClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions).setAgentObjectId(testResourceNamer.randomUuid()),
            key);

        StepVerifier.create(getFileAsyncClient(sasWithoutPermission, getFileSystemUrl(), pathName).getProperties())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void fileSystemUserDelegationCorrelationId() {
        FileSystemSasPermission permissions = new FileSystemSasPermission().setListPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime);
        key.setSignedObjectId(testResourceNamer.recordValueFromConfig(key.getSignedObjectId()));
        key.setSignedTenantId(testResourceNamer.recordValueFromConfig(key.getSignedTenantId()));
        String cid = testResourceNamer.randomUuid();

        String sasWithPermissions = dataLakeFileSystemAsyncClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions).setCorrelationId(cid), key);

        DataLakeFileSystemAsyncClient client = getFileSystemAsyncClient(sasWithPermissions, getFileSystemUrl());

        StepVerifier.create(client.listPaths())
            .expectNextCount(1)
            .verifyComplete();
        assertTrue(sasWithPermissions.contains("scid=" + cid));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void fileSystemUserDelegationCorrelationIdError() {
        FileSystemSasPermission permissions = new FileSystemSasPermission().setListPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime);
        key.setSignedObjectId(testResourceNamer.recordValueFromConfig(key.getSignedObjectId()));
        key.setSignedTenantId(testResourceNamer.recordValueFromConfig(key.getSignedTenantId()));
        String cid = "invalidcid";

        String sasWithPermissions = dataLakeFileSystemAsyncClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions).setCorrelationId(cid), key);

        DataLakeFileSystemAsyncClient client = getFileSystemAsyncClient(sasWithPermissions, getFileSystemUrl());

        StepVerifier.create(client.listPaths())
            .verifyError(DataLakeStorageException.class);
        assertTrue(sasWithPermissions.contains("scid=" + cid));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void accountSasFileRead() {
        String pathName = generatePathName();
        DataLakeFileAsyncClient fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(pathName);

        Mono<PathInfo> step = fc.create()
            .then(fc.append(DATA.getDefaultBinaryData(), 0))
            .then(fc.flush(DATA.getDefaultDataSizeLong(), true));

        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        String sas = primaryDataLakeServiceAsyncClient.generateAccountSas(
            new AccountSasSignatureValues(expiryTime, permissions, service, resourceType));
        BlockBlobAsyncClient client = getFileAsyncClient(sas, getFileSystemUrl(), pathName).getBlockBlobAsyncClient();

        StepVerifier.create(step.then(FluxUtil.collectBytesInByteBufferStream(client.download())))
            .assertNext(r -> assertArrayEquals(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @Test
    public void accountSasFileDeleteError() {
        String pathName = generatePathName();
        DataLakeFileAsyncClient fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(pathName);

        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);

        String sas = primaryDataLakeServiceAsyncClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        DataLakeFileAsyncClient client = getFileAsyncClient(sas, getFileSystemUrl(), pathName);

        StepVerifier.create(fc.create().then(client.delete()))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void accountSasCreateFileSystemError() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true).setCreatePermission(false);

        String sas = primaryDataLakeServiceAsyncClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        DataLakeServiceAsyncClient sc = getServiceAsyncClient(sas, primaryDataLakeServiceAsyncClient.getAccountUrl());

        StepVerifier.create(sc.createFileSystem(generateFileSystemName()))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void accountSasCreateFileSystem() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true).setCreatePermission(true);

        String sas = primaryDataLakeServiceAsyncClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        DataLakeServiceAsyncClient sc = getServiceAsyncClient(sas, primaryDataLakeServiceAsyncClient.getAccountUrl());

        StepVerifier.create(sc.createFileSystem(generateFileSystemName()))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void accountSasTokenOnEndpoint() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true).setCreatePermission(true)
            .setListPermission(true);

        String sas = primaryDataLakeServiceAsyncClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        String fileSystemName = generateFileSystemName();

        StepVerifier.create(getServiceClientBuilder(null, primaryDataLakeServiceAsyncClient.getAccountUrl() + "?" + sas)
            .buildAsyncClient()
            .createFileSystem(fileSystemName)
            .thenMany(getFileSystemClientBuilder(
                primaryDataLakeServiceAsyncClient.getAccountUrl() + "/" + fileSystemName + "?" + sas)
                .buildAsyncClient().listPaths()))
            .verifyComplete();

        StepVerifier.create(getFileAsyncClient(
            getDataLakeCredential(), primaryDataLakeServiceAsyncClient
            .getAccountUrl() + "/" + fileSystemName + "/" + generatePathName() + "?" + sas)
            .create())
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void canUseSasToAuthenticate() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);
        String sas = primaryDataLakeServiceAsyncClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        String pathName = generatePathName();
        Mono<DataLakeDirectoryAsyncClient> step = dataLakeFileSystemAsyncClient.createDirectory(pathName);
        Mono<String> fileSystemUrl = Mono.just(dataLakeFileSystemAsyncClient.getFileSystemUrl());

        StepVerifier.create(step.then(fileSystemUrl).flatMap(r -> instrument(new DataLakeFileSystemClientBuilder().endpoint(r).sasToken(sas))
            .buildAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakeFileSystemClientBuilder()
            .endpoint(r)
            .credential(new AzureSasCredential(sas))).buildAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakeFileSystemClientBuilder().endpoint(r + "?" + sas))
            .buildAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakePathClientBuilder().endpoint(r).pathName(pathName)
            .sasToken(sas)).buildDirectoryAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakePathClientBuilder().endpoint(r)
            .pathName(pathName).credential(new AzureSasCredential(sas))).buildDirectoryAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakePathClientBuilder().endpoint(r + "?" + sas)
            .pathName(pathName)).buildDirectoryAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakePathClientBuilder().endpoint(r).pathName(pathName)
            .sasToken(sas)).buildFileAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakePathClientBuilder().endpoint(r).pathName(pathName)
            .credential(new AzureSasCredential(sas))).buildFileAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakePathClientBuilder().endpoint(r + "?" + sas)
            .pathName(pathName)).buildFileAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakeServiceClientBuilder().endpoint(r).sasToken(sas))
            .buildAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakeServiceClientBuilder().endpoint(r)
            .credential(new AzureSasCredential(sas))).buildAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fileSystemUrl.flatMap(r -> instrument(new DataLakeServiceClientBuilder().endpoint(r + "?" + sas))
            .buildAsyncClient().getProperties()))
            .expectNextCount(1)
            .verifyComplete();
    }


}
