// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.credential.AzureSasCredential;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.datalake.implementation.util.DataLakeSasImplUtil;
import com.azure.storage.file.datalake.models.AccessControlType;
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemAccessPolicies;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.RolePermissions;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import com.azure.storage.file.datalake.sas.PathSasPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SasTests extends DataLakeTestBase {
    private DataLakeFileClient sasClient;
    private String pathName;

    @BeforeEach
    public void setup() {
        pathName = generatePathName();
        sasClient = getFileClient(getDataLakeCredential(), getFileSystemUrl(), pathName);
        sasClient.create();
        sasClient.append(DATA.getDefaultBinaryData(), 0);
        sasClient.flush(DATA.getDefaultDataSizeLong(), true);
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

        DataLakeFileClient client = getFileClient(sasClient.generateSas(generateValues(permissions)),
            getFileSystemUrl(), pathName);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        client.read(os);

        assertEquals(DATA.getDefaultText(), os.toString());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void directorySasPermission() {
        String pathName = generatePathName();
        DataLakeDirectoryClient sasClient = getDirectoryClient(getDataLakeCredential(), getFileSystemUrl(), pathName);
        sasClient.create();

        String sas = sasClient.generateSas(generateValues(new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setManageOwnershipPermission(true)
            .setManageAccessControlPermission(true)));

        DataLakeDirectoryClient client = getDirectoryClient(sas, getFileSystemUrl(), pathName);

        PathProperties properties = assertDoesNotThrow(() -> client.getProperties());
        validateSasProperties(properties);

        assertDoesNotThrow(() -> client.createSubdirectory(generatePathName()));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void directorySasPermissionFail() {
        String pathName = generatePathName();
        DataLakeDirectoryClient sasClient = getDirectoryClient(getDataLakeCredential(), getFileSystemUrl(), pathName);
        sasClient.create();

        String sas = sasClient.generateSas(generateValues(new PathSasPermission() /* No read permission. */
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)));

        assertThrows(DataLakeStorageException.class, () -> getDirectoryClient(sas, getFileSystemUrl(), pathName).getProperties());
    }

    @Test
    public void fileSystemSasIdentifier() {
        DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier().setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)));
        dataLakeFileSystemClient.setAccessPolicy(null, Collections.singletonList(identifier));

        // Wait 30 seconds as it may take time for the access policy to take effect.
        waitUntilPredicate(1000, 30, () -> {
            FileSystemAccessPolicies accessPolicies = dataLakeFileSystemClient.getAccessPolicy();

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
        DataLakeFileSystemClient client1 = getFileSystemClient(dataLakeFileSystemClient.generateSas(sasValues), getFileSystemUrl());

        assertDoesNotThrow(() -> client1.listPaths().iterator().hasNext());

        sasValues = new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions);
        DataLakeFileSystemClient client2 = getFileSystemClient(dataLakeFileSystemClient.generateSas(sasValues), getFileSystemUrl());

        assertDoesNotThrow(() -> client2.listPaths().iterator().hasNext());
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

        DataLakeFileClient client = getFileClient(sasClient.generateUserDelegationSas(generateValues(permissions),
            getUserDelegationInfo()), getFileSystemUrl(), pathName);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        client.read(os);

        assertEquals(DATA.getDefaultText(), os.toString());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void directoryUserDelegation() {
        String pathName = generatePathName();
        DataLakeDirectoryClient sasClient = getDirectoryClient(getDataLakeCredential(), getFileSystemUrl(), pathName);
        sasClient.create();
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

        String sas = sasClient.generateUserDelegationSas(generateValues(permissions), getUserDelegationInfo());

        DataLakeDirectoryClient client = getDirectoryClient(sas, getFileSystemUrl(), pathName);

        PathProperties properties = assertDoesNotThrow(() -> client.getProperties());
        validateSasProperties(properties);

        assertDoesNotThrow(() -> client.createSubdirectory(generatePathName()));

        dataLakeFileSystemClient = getFileSystemClient(sas, getFileSystemUrl());
        Iterator<?> it = dataLakeFileSystemClient.listPaths(new ListPathsOptions().setPath(pathName), null).iterator();

        assertNotNull(it.next());
        assertFalse(it.hasNext());
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

        String sasWithPermissions = dataLakeFileSystemClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions), key);

        DataLakeFileSystemClient client = getFileSystemClient(sasWithPermissions, getFileSystemUrl());

        assertDoesNotThrow(() -> client.listPaths().iterator().hasNext());
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
        DataLakeDirectoryClient rootClient = getDirectoryClient(getDataLakeCredential(), getFileSystemUrl(), "");
        rootClient.setAccessControlList(Collections.singletonList(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setEntityId(saoid)
            .setPermissions(RolePermissions.parseSymbolic("rwx", false))), null, null);

        String sasWithPermissions = rootClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions).setPreauthorizedAgentObjectId(saoid), key);

        DataLakeFileClient client = getFileClient(sasWithPermissions, getFileSystemUrl(), pathName);
        client.create(true);
        client.append(DATA.getDefaultBinaryData(), 0);
        client.flush(DATA.getDefaultDataSizeLong(), true);

        assertTrue(sasWithPermissions.contains("saoid=" + saoid));

        client = getFileClient(getDataLakeCredential(), getFileSystemUrl(), pathName);

        PathAccessControl accessControl = assertDoesNotThrow(client::getAccessControl);
        assertEquals(saoid, accessControl.getOwner());
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
        DataLakeFileClient client = getFileClient(sasWithPermissions, getFileSystemUrl(), pathName);

        try {
            client.create(true);
            client.append(DATA.getDefaultBinaryData(), 0);
            client.flush(DATA.getDefaultDataSizeLong(), true);
            fail("User should not be authorized yet.");
        } catch (Exception e) {
            assertInstanceOf(DataLakeStorageException.class, e);
        }

        assertTrue(sasWithPermissions.contains("suoid=" + suoid));

        // User is now authorized.
        /* Grant userOID on root folder. */
        DataLakeDirectoryClient rootClient = getDirectoryClient(getDataLakeCredential(), getFileSystemUrl(), "");
        rootClient.setAccessControlList(Collections.singletonList(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setEntityId(suoid)
            .setPermissions(RolePermissions.parseSymbolic("rwx", false))), null, null);

        sasWithPermissions = rootClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions).setAgentObjectId(suoid), key);

        client = getFileClient(sasWithPermissions, getFileSystemUrl(), pathName);
        client.create(true);
        client.append(DATA.getDefaultBinaryData(), 0);
        client.flush(DATA.getDefaultDataSizeLong(), true);

        client = getFileClient(getDataLakeCredential(), getFileSystemUrl(), pathName);
        assertTrue(sasWithPermissions.contains("suoid=" + suoid));
        assertEquals(suoid, client.getAccessControl().getOwner());

        // Use random other suoid. User should not be authorized.
        String sasWithoutPermission = rootClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions).setAgentObjectId(testResourceNamer.randomUuid()), key);

        assertThrows(DataLakeStorageException.class, () ->
            getFileClient(sasWithoutPermission, getFileSystemUrl(), pathName).getProperties());
    }

    @Test
    public void fileSystemUserDelegationCorrelationId() {
        FileSystemSasPermission permissions = new FileSystemSasPermission().setListPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime);
        key.setSignedObjectId(testResourceNamer.recordValueFromConfig(key.getSignedObjectId()));
        key.setSignedTenantId(testResourceNamer.recordValueFromConfig(key.getSignedTenantId()));
        String cid = testResourceNamer.randomUuid();

        String sasWithPermissions = dataLakeFileSystemClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions).setCorrelationId(cid), key);

        DataLakeFileSystemClient client = getFileSystemClient(sasWithPermissions, getFileSystemUrl());

        assertDoesNotThrow(() -> client.listPaths().iterator().hasNext());
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

        String sasWithPermissions = dataLakeFileSystemClient.generateUserDelegationSas(
            new DataLakeServiceSasSignatureValues(expiryTime, permissions).setCorrelationId(cid), key);

        DataLakeFileSystemClient client = getFileSystemClient(sasWithPermissions, getFileSystemUrl());

        assertThrows(DataLakeStorageException.class, () -> client.listPaths().iterator().hasNext());
        assertTrue(sasWithPermissions.contains("scid=" + cid));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void accountSasFileRead() {
        String pathName = generatePathName();
        DataLakeFileClient fc = dataLakeFileSystemClient.getFileClient(pathName);
        fc.create();
        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        String sas = primaryDataLakeServiceClient.generateAccountSas(
            new AccountSasSignatureValues(expiryTime, permissions, service, resourceType));
        BlockBlobClient client = getFileClient(sas, getFileSystemUrl(), pathName).getBlockBlobClient();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        client.download(os);

        assertEquals(DATA.getDefaultText(), os.toString());
    }

    @Test
    public void accountSasFileDeleteError() {
        String pathName = generatePathName();
        DataLakeFileClient fc = dataLakeFileSystemClient.getFileClient(pathName);
        fc.create();

        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);

        String sas = primaryDataLakeServiceClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        DataLakeFileClient client = getFileClient(sas, getFileSystemUrl(), pathName);

        assertThrows(DataLakeStorageException.class, client::delete);
    }

    @Test
    public void accountSasCreateFileSystemError() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true).setCreatePermission(false);

        String sas = primaryDataLakeServiceClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        DataLakeServiceClient sc = getServiceClient(sas, primaryDataLakeServiceClient.getAccountUrl());

        assertThrows(DataLakeStorageException.class, () -> sc.createFileSystem(generateFileSystemName()));
    }

    @Test
    public void accountSasCreateFileSystem() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true).setCreatePermission(true);

        String sas = primaryDataLakeServiceClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        DataLakeServiceClient sc = getServiceClient(sas, primaryDataLakeServiceClient.getAccountUrl());

        assertDoesNotThrow(() -> sc.createFileSystem(generateFileSystemName()));
    }

    @Test
    public void accountSasTokenOnEndpoint() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true).setCreatePermission(true)
            .setListPermission(true);

        String sas = primaryDataLakeServiceClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        String fileSystemName = generateFileSystemName();

        getServiceClientBuilder(null, primaryDataLakeServiceClient.getAccountUrl() + "?" + sas)
            .buildClient()
            .createFileSystem(fileSystemName);

        assertDoesNotThrow(() -> getFileSystemClientBuilder(primaryDataLakeServiceClient.getAccountUrl() + "/" + fileSystemName + "?" + sas)
            .buildClient().listPaths().iterator().hasNext());

        assertDoesNotThrow(() -> getFileClient(getDataLakeCredential(),
            primaryDataLakeServiceClient.getAccountUrl() + "/" + fileSystemName + "/" + generatePathName() + "?" + sas)
                .create());
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("sasImplUtilStringToSignSupplier")
    public void sasImplUtilStringToSign(OffsetDateTime startTime, String identifier, SasIpRange ipRange,
        SasProtocol protocol, String cacheControl, String disposition, String encoding, String language, String type,
        String expectedStringToSign) {
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        PathSasPermission p = new PathSasPermission().setReadPermission(true);
        DataLakeServiceSasSignatureValues v = (identifier != null)
            ? new DataLakeServiceSasSignatureValues(identifier)
            : new DataLakeServiceSasSignatureValues(e, p);
        String expected = String.format(expectedStringToSign, ENVIRONMENT.getDataLakeAccount().getName());

        v.setPermissions(p).setStartTime(startTime).setExpiryTime(e);
        if (ipRange != null) {
            v.setSasIpRange(new SasIpRange().setIpMin("ip"));
        }

        v.setIdentifier(identifier)
            .setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type);

        DataLakeSasImplUtil util = new DataLakeSasImplUtil(v, "fileSystemName", "pathName", false);
        util.ensureState();

        assertEquals(expected, util.stringToSign(util.getCanonicalName(ENVIRONMENT.getDataLakeAccount().getName())));
    }

    private static Stream<Arguments> sasImplUtilStringToSignSupplier() {
        // We don't test the blob or containerName properties because canonicalized resource is always added as at least
        // /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        // sas but the construction of the string to sign.
        // Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
        return Stream.of(
            // startTime | identifier | ipRange | protocol | cacheControl | disposition | encoding | language | type | expectedStringToSign
            Arguments.of(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null, null, null, null, null, null,
                null, null, "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, "id", null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\nid\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, new SasIpRange(), null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, SasProtocol.HTTPS_ONLY, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, "control", null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\ncontrol\n\n\n\n"),
            Arguments.of(null, null, null, null, null, "disposition", null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\ndisposition\n\n\n"),
            Arguments.of(null, null, null, null, null, null, "encoding", null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\nencoding\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "language", null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\nlanguage\n"),
            Arguments.of(null, null, null, null, null, null, null, null, "type", "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\ntype")
        );
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("sasImplUtilStringToSignUserDelegationKeySupplier")
    public void sasImplUtilStringToSignUserDelegationKey(OffsetDateTime startTime, String keyOid, String keyTid,
        OffsetDateTime keyStart, OffsetDateTime keyExpiry, String keyService, String keyVersion, String keyValue,
        SasIpRange ipRange, SasProtocol protocol, String cacheControl, String disposition, String encoding,
        String language, String type, String saoid, String suoid, String cid, String expectedStringToSign) {

        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        PathSasPermission p = new PathSasPermission().setReadPermission(true);
        String expected = String.format(expectedStringToSign, ENVIRONMENT.getDataLakeAccount().getName());

        DataLakeServiceSasSignatureValues v = new DataLakeServiceSasSignatureValues(e, p)
            .setPermissions(p)
            .setStartTime(startTime)
            .setExpiryTime(e)
            .setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type)
            .setCorrelationId(cid)
            .setPreauthorizedAgentObjectId(saoid)
            .setAgentObjectId(suoid);

        if (ipRange != null) {
            v.setSasIpRange(new SasIpRange().setIpMin("ip"));
        }

        UserDelegationKey key = new UserDelegationKey()
            .setSignedObjectId(keyOid)
            .setSignedTenantId(keyTid)
            .setSignedStart(keyStart)
            .setSignedExpiry(keyExpiry)
            .setSignedService(keyService)
            .setSignedVersion(keyVersion)
            .setValue(keyValue);

        DataLakeSasImplUtil util = new DataLakeSasImplUtil(v, "fileSystemName", "pathName", false);
        util.ensureState();

        assertEquals(expected, util.stringToSign(key, util.getCanonicalName(ENVIRONMENT.getDataLakeAccount().getName())));
    }

    private static Stream<Arguments> sasImplUtilStringToSignUserDelegationKeySupplier() {
        // We test string to sign functionality directly related to user delegation sas specific parameters
        return Stream.of(
            // startTime | keyOid | keyTid | keyStart | keyExpiry | keyService | keyVersion | keyValue | ipRange | protocol | cacheControl | disposition | encoding | language | type | saoid | suoid | cid | expectedStringToSign
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null, null, null, null, null, null,
                "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null,
                null, "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, "11111111-1111-1111-1111-111111111111", null, null, null, null, null,
                "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null,
                null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, "22222222-2222-2222-2222-222222222222", null, null, null, null,
                "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null,
                null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC), null,
                null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null,
                null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC),
                null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null,
                null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, "b", null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\nb\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, "2018-06-17",
                "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null,
                null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n2018-06-17\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=",
                new SasIpRange(), null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                SasProtocol.HTTPS_ONLY, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, "control", null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\ncontrol\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, null, "disposition", null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\ndisposition\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, null, null, "encoding", null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\nencoding\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, null, null, null, "language", null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\nlanguage\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, null, null, null, null, "type", null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\ntype"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, null, null, null, null, null, "saoid", null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\nsaoid\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, null, null, null, null, null, null, "suoid", null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\nsuoid\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null,
                null, null, null, null, null, null, null, null, "cid", "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\ncid\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n")
        );
    }

    @Test
    public void canUseSasToAuthenticate() {
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);
        String sas = primaryDataLakeServiceClient.generateAccountSas(
            new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType));
        String pathName = generatePathName();
        dataLakeFileSystemClient.createDirectory(pathName);
        String fileSystemUrl = dataLakeFileSystemClient.getFileSystemUrl();

        assertDoesNotThrow(() -> instrument(new DataLakeFileSystemClientBuilder().endpoint(fileSystemUrl).sasToken(sas))
                .buildClient().getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakeFileSystemClientBuilder()
            .endpoint(fileSystemUrl)
            .credential(new AzureSasCredential(sas))).buildClient().getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakeFileSystemClientBuilder().endpoint(fileSystemUrl + "?" + sas))
            .buildClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakePathClientBuilder().endpoint(fileSystemUrl).pathName(pathName)
                .sasToken(sas)).buildDirectoryClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakePathClientBuilder()
            .endpoint(fileSystemUrl)
            .pathName(pathName)
            .credential(new AzureSasCredential(sas))).buildDirectoryClient().getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakePathClientBuilder().endpoint(fileSystemUrl + "?" + sas)
                .pathName(pathName)).buildDirectoryClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakePathClientBuilder().endpoint(fileSystemUrl).pathName(pathName)
                .sasToken(sas)).buildFileClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakePathClientBuilder()
            .endpoint(fileSystemUrl)
            .pathName(pathName)
            .credential(new AzureSasCredential(sas))).buildFileClient().getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakePathClientBuilder().endpoint(fileSystemUrl + "?" + sas)
                .pathName(pathName)).buildFileClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakeServiceClientBuilder().endpoint(fileSystemUrl).sasToken(sas))
            .buildClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakeServiceClientBuilder().endpoint(fileSystemUrl)
                .credential(new AzureSasCredential(sas))).buildClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new DataLakeServiceClientBuilder().endpoint(fileSystemUrl + "?" + sas))
            .buildClient()
            .getProperties());
    }

}
