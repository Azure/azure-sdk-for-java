// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.file.share.models.ShareAccessPolicy;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileSasTests extends FileShareTestBase {
    private ShareFileClient primaryFileClient;
    private ShareClient primaryShareClient;
    private ShareServiceClient primaryFileServiceClient;
    private String shareName;

    private final String filePath = "filename";

    @BeforeEach
    public void setup() {
        shareName = generateShareName();

        primaryFileServiceClient = fileServiceBuilderHelper().buildClient();
        primaryShareClient = shareBuilderHelper(shareName).buildClient();
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();
    }

    @ParameterizedTest
    @MethodSource("fileSASPermissionsToStringSupplier")
    public void fileSASPermissionsToString(boolean read, boolean write, boolean delete, boolean create,
        String expectedString) {
        ShareFileSasPermission perms = new ShareFileSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create);

        assertEquals(perms.toString(), expectedString);
    }

    private static Stream<Arguments> fileSASPermissionsToStringSupplier() {
        return Stream.of(Arguments.of(true, false, false, false, "r"),
            Arguments.of(false, true, false, false, "w"),
            Arguments.of(false, false, true, false, "d"),
            Arguments.of(false, false, false, true, "c"),
            Arguments.of(true, true, true, true, "rcwd"));
    }

    @ParameterizedTest
    @MethodSource("fileSASPermissionsParseSupplier")
    public void fileSASPermissionsParse(String permString, boolean read, boolean write, boolean delete,
        boolean create) {
        ShareFileSasPermission perms = ShareFileSasPermission.parse(permString);

        assertEquals(perms.hasReadPermission(), read);
        assertEquals(perms.hasWritePermission(), write);
        assertEquals(perms.hasDeletePermission(), delete);
        assertEquals(perms.hasCreatePermission(), create);
    }

    private static Stream<Arguments> fileSASPermissionsParseSupplier() {
        return Stream.of(
            Arguments.of("r", true, false, false, false),
            Arguments.of("w", false, true, false, false),
            Arguments.of("d", false, false, true, false),
            Arguments.of("c", false, false, false, true),
            Arguments.of("rcwd", true, true, true, true),
            Arguments.of("dcwr", true, true, true, true));
    }

    @Test
    public void fileSASPermissionsParseIA() {
        assertThrows(IllegalArgumentException.class, () -> ShareFileSasPermission.parse("rwaq"));
    }

    @ParameterizedTest
    @MethodSource("shareSASPermissionsToStringSupplier")
    public void shareSASPermissionsToString(boolean read, boolean write, boolean delete, boolean create, boolean list,
        String expectedString) {
        ShareSasPermission perms = new ShareSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setListPermission(list);

        assertEquals(perms.toString(), expectedString);
    }

    private static Stream<Arguments> shareSASPermissionsToStringSupplier() {
        return Stream.of(Arguments.of(true, false, false, false, false, "r"),
            Arguments.of(false, true, false, false, false, "w"),
            Arguments.of(false, false, true, false, false, "d"),
            Arguments.of(false, false, false, true, false, "c"),
            Arguments.of(false, false, false, false, true, "l"),
            Arguments.of(true, true, true, true, true, "rcwdl"));
    }

    @ParameterizedTest
    @MethodSource("shareSASPermissionsParseSupplier")
    public void shareSASPermissionsParse(String permString, boolean read, boolean write, boolean delete, boolean create,
        boolean list) {
        ShareSasPermission perms = ShareSasPermission.parse(permString);

        assertEquals(perms.hasReadPermission(), read);
        assertEquals(perms.hasWritePermission(), write);
        assertEquals(perms.hasDeletePermission(), delete);
        assertEquals(perms.hasCreatePermission(), create);
        assertEquals(perms.hasListPermission(), list);
    }

    private static Stream<Arguments> shareSASPermissionsParseSupplier() {
        return Stream.of(Arguments.of("r", true, false, false, false, false),
            Arguments.of("w", false, true, false, false, false),
            Arguments.of("d", false, false, true, false, false),
            Arguments.of("c", false, false, false, true, false),
            Arguments.of("l", false, false, false, false, true),
            Arguments.of("rcwdl", true, true, true, true, true),
            Arguments.of("dcwrl", true, true, true, true, true));
    }

    @Test
    public void shareSASPermissionsParseIA() {
        assertThrows(IllegalArgumentException.class, () -> ShareSasPermission.parse("rwaq"));
    }

    @Test
    public void fileSASNetworkTestDownloadUpload() {
        String data = "test";
        primaryShareClient.create();
        primaryFileClient.create(Constants.KB);
        primaryFileClient.uploadRange(FileShareTestHelper.getInputStream(data.getBytes()), (long) data.length());

        ShareFileSasPermission permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true);

        OffsetDateTime startTime = testResourceNamer.now().minusDays(1);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        SasProtocol sasProtocol = SasProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";

        StorageSharedKeyCredential credential = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sas = new ShareServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        assertNotNull(sas);

        ShareFileClient client = fileBuilderHelper(shareName, filePath)
            .endpoint(primaryFileClient.getFileUrl())
            .sasToken(sas)
            .buildFileClient();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        client.download(stream);

        client.uploadRange(FileShareTestHelper.getInputStream(data.getBytes(StandardCharsets.UTF_8)), data.length());
        assertArrayEquals(Arrays.copyOfRange(stream.toByteArray(), 0, data.length()),
            data.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void fileSASNetworkTestUploadFails() {
        String data = "test";
        primaryShareClient.create();
        primaryFileClient.create(Constants.KB);

        ShareFileSasPermission permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(false)
            .setCreatePermission(true)
            .setDeletePermission(true);
        OffsetDateTime startTime = testResourceNamer.now().minusDays(1);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        SasProtocol sasProtocol = SasProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";

        StorageSharedKeyCredential credential = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sas = new ShareServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        ShareFileClient client = fileBuilderHelper(shareName, filePath)
            .endpoint(primaryFileClient.getFileUrl())
            .sasToken(sas)
            .buildFileClient();

        assertThrows(ShareStorageException.class, () -> client.uploadRange(
            FileShareTestHelper.getInputStream(data.getBytes()), data.length()));
        assertDoesNotThrow(client::delete);
    }

    @Test
    public void shareSASNetworkIdentifierPermissions() {
        ShareSignedIdentifier identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy().setPermissions("rcwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)));
        primaryShareClient.create();
        primaryShareClient.setAccessPolicy(Arrays.asList(identifier));

        // Check containerSASPermissions
        ShareSasPermission permissions = new ShareSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setListPermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        StorageSharedKeyCredential credential = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasWithId = new ShareServiceSasSignatureValues()
            .setIdentifier(identifier.getId())
            .setShareName(primaryShareClient.getShareName())
            .generateSasQueryParameters(credential)
            .encode();

        ShareClient client1 = shareBuilderHelper(primaryShareClient.getShareName())
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sasWithId)
            .buildClient();

        String dirName = generatePathName();
        client1.createDirectory(dirName);
        client1.deleteDirectory(dirName);

        String sasWithPermissions = new ShareServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setShareName(primaryFileClient.getShareName())
            .generateSasQueryParameters(credential)
            .encode();

        ShareClient client2 = shareBuilderHelper(primaryShareClient.getShareName())
            .endpoint(primaryFileClient.getFileUrl())
            .sasToken(sasWithPermissions)
            .buildClient();

        String dirName2 = generatePathName();
        client2.createDirectory(dirName2);
        assertDoesNotThrow(() -> client2.deleteDirectory(dirName2));
    }

    @Test
    public void accountSASNetworkCreateDeleteShare() {
        AccountSasService service = new AccountSasService()
            .setFileAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        StorageSharedKeyCredential credential = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = fileServiceBuilderHelper()
            .endpoint(primaryFileServiceClient.getFileServiceUrl())
            .credential(credential)
            .buildClient()
            .generateAccountSas(sasValues);

        assertNotNull(sas);
        ShareServiceClient sc = fileServiceBuilderHelper().endpoint(primaryFileServiceClient.getFileServiceUrl())
            .sasToken(sas).buildClient();
        String shareName = generateShareName();
        sc.createShare(shareName);
        assertDoesNotThrow(() -> sc.deleteShare(shareName));
    }

    @Test
    public void accountSASNetworkAccountSasTokenOnEndpoint() {
        AccountSasService service = new AccountSasService()
            .setFileAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        StorageSharedKeyCredential credential = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = fileServiceBuilderHelper()
            .endpoint(primaryFileServiceClient.getFileServiceUrl())
            .credential(credential)
            .buildClient()
            .generateAccountSas(sasValues);

        String shareName = generateShareName();
        String pathName = generatePathName();

        ShareServiceClient sc = getServiceClientBuilder(null, primaryFileServiceClient.getFileServiceUrl() + "?" + sas,
            (HttpPipelinePolicy) null).buildClient();
        sc.createShare(shareName);

        ShareClient sharec = getShareClientBuilder(primaryFileServiceClient.getFileServiceUrl() + "/" + shareName + "?"
            + sas).buildClient();
        sharec.createFile(pathName, 1024);

        ShareFileClient fc = getFileClient((StorageSharedKeyCredential) null,
            primaryFileServiceClient.getFileServiceUrl() + "/" + shareName + "/" + pathName + "?" + sas);
        assertDoesNotThrow(() -> fc.download(new ByteArrayOutputStream()));
    }

    /*
    Ensures that we don't break the functionality of the deprecated means of generating an AccountSas.
    Only run in live mode because recordings would frequently get messed up when we update recordings to new sas version
     */
    @EnabledIf("com.azure.storage.file.share.FileShareTestBase#isLiveMode")
    @Test
    public void accountSasDeprecated() {
        AccountSasService service = new AccountSasService()
            .setFileAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        StorageSharedKeyCredential credential = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(credential)
            .encode();

        assertNotNull(sas);

        ShareServiceClientBuilder scBuilder = fileServiceBuilderHelper();
        scBuilder.endpoint(primaryFileServiceClient.getFileServiceUrl())
            .sasToken(sas);
        ShareServiceClient sc = scBuilder.buildClient();
        String shareName = generateShareName();
        sc.createShare(shareName);
        assertDoesNotThrow(() -> sc.deleteShare(shareName));
    }

    @Test
    public void parseProtocol() {
        primaryShareClient.create();
        primaryFileClient.create(100);
        String sas = primaryFileServiceClient.generateAccountSas(new AccountSasSignatureValues(
            testResourceNamer.now().plusDays(1),
            AccountSasPermission.parse("r"), new AccountSasService().setFileAccess(true),
            new AccountSasResourceType().setService(true).setContainer(true).setObject(true))
            .setProtocol(SasProtocol.HTTPS_HTTP));

        ShareFileClient sasClient = instrument(new ShareFileClientBuilder()
            .endpoint(primaryFileClient.getFileUrl() + "?" + sas))
            .buildFileClient();

        assertNotNull(sasClient.getProperties());
        ShareClient sasShareClient = instrument(new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl() + "?" + sas))
            .buildClient();

        assertNotNull(sasShareClient.getProperties());

        ShareServiceClient sasServiceClient = instrument(new ShareServiceClientBuilder()
            .endpoint(primaryFileServiceClient.getFileServiceUrl() + "?" + sas))
            .buildClient();
        assertNotNull(sasServiceClient.getProperties());
    }

    @Test
    public void canUseSasToAuthenticate() {
        AccountSasService service = new AccountSasService()
            .setFileAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = primaryFileServiceClient.generateAccountSas(sasValues);
        String pathName = generatePathName();
        primaryShareClient.create();
        primaryShareClient.createDirectory(pathName);

        assertDoesNotThrow(() -> instrument(new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl() + "?" + sas))
            .buildClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new ShareFileClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .resourcePath(pathName)
            .sasToken(sas))
            .buildDirectoryClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new ShareFileClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .resourcePath(pathName)
            .credential(new AzureSasCredential(sas)))
            .buildDirectoryClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new ShareFileClientBuilder()
            .endpoint(primaryShareClient.getShareUrl() + "?" + sas)
            .resourcePath(pathName))
            .buildDirectoryClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new ShareServiceClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new ShareServiceClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties());

        assertDoesNotThrow(() -> instrument(new ShareServiceClientBuilder()
            .endpoint(primaryShareClient.getShareUrl() + "?" + sas))
            .buildClient()
            .getProperties());
    }
}
