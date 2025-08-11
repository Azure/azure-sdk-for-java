// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.models.ShareAccessPolicy;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.UserDelegationKey;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static com.azure.storage.common.test.shared.StorageCommonTestUtils.getOidFromToken;
import static com.azure.storage.file.share.FileShareTestHelper.assertExceptionStatusCodeAndMessage;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSasClientTests extends FileShareTestBase {

    private ShareFileClient primaryFileClient;
    private ShareClient primaryShareClient;
    private ShareServiceClient primaryFileServiceClient;
    private String shareName;

    private final String filePath = "filename";
    private String data;

    @BeforeEach
    public void setup() {
        shareName = generateShareName();

        primaryFileServiceClient = fileServiceBuilderHelper().buildClient();
        primaryShareClient = shareBuilderHelper(shareName).buildClient();
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();

        data = "test";
        primaryShareClient.create();
        primaryFileClient.create(Constants.KB);
    }

    ShareServiceSasSignatureValues generateValues(ShareFileSasPermission permission) {
        return new ShareServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setCacheControl("cache")
            .setContentDisposition("disposition")
            .setContentEncoding("encoding")
            .setContentLanguage("language")
            .setContentType("type");
    }

    @Test
    public void fileSASNetworkTestDownloadUpload() {
        primaryFileClient.uploadRange(FileShareTestHelper.getInputStream(data.getBytes()), data.length());
        ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true);
        ShareServiceSasSignatureValues sasValues = generateValues(permissions);

        String sas = primaryFileClient.generateSas(sasValues);
        ShareFileClient client = fileBuilderHelper(shareName, filePath).endpoint(primaryFileClient.getFileUrl())
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
        ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true)
            .setWritePermission(false)
            .setCreatePermission(true)
            .setDeletePermission(true);
        ShareServiceSasSignatureValues sasValues = generateValues(permissions);
        String sas = primaryFileClient.generateSas(sasValues);
        ShareFileClient client = fileBuilderHelper(shareName, filePath).endpoint(primaryFileClient.getFileUrl())
            .sasToken(sas)
            .buildFileClient();
        assertThrows(ShareStorageException.class,
            () -> client.uploadRange(FileShareTestHelper.getInputStream(data.getBytes()), data.length()));
        assertDoesNotThrow(client::delete);
    }

    @Test
    public void shareSASNetworkIdentifierPermissions() {
        ShareSignedIdentifier identifier = new ShareSignedIdentifier().setId("0000")
            .setAccessPolicy(
                new ShareAccessPolicy().setPermissions("rcwdl").setExpiresOn(testResourceNamer.now().plusDays(1)));

        primaryShareClient.setAccessPolicy(Arrays.asList(identifier));

        // Sleep 30 seconds if running against the live service as it may take ACLs that long to take effect.
        sleepIfRunningAgainstService(30000);

        // Check shareSASPermissions
        ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setListPermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        ShareServiceSasSignatureValues sasValues = new ShareServiceSasSignatureValues(identifier.getId());
        String sasWithId = primaryShareClient.generateSas(sasValues);

        ShareClient client1
            = shareBuilderHelper(primaryShareClient.getShareName()).endpoint(primaryShareClient.getShareUrl())
                .sasToken(sasWithId)
                .buildClient();

        client1.createDirectory("dir");
        client1.deleteDirectory("dir");

        sasValues = new ShareServiceSasSignatureValues(expiryTime, permissions);
        String sasWithPermissions = primaryShareClient.generateSas(sasValues);
        ShareClient client2
            = shareBuilderHelper(primaryShareClient.getShareName()).endpoint(primaryFileClient.getFileUrl())
                .sasToken(sasWithPermissions)
                .buildClient();

        client2.createDirectory("dir");
        assertDoesNotThrow(() -> client2.deleteDirectory("dir"));
    }

    @Test
    public void accountSASNetworkCreateDeleteShare() {
        AccountSasService service = new AccountSasService().setFileAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions
            = new AccountSasPermission().setReadPermission(true).setCreatePermission(true).setDeletePermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        AccountSasSignatureValues sasValues
            = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType);
        String sas = primaryFileServiceClient.generateAccountSas(sasValues);
        ShareServiceClient sc = fileServiceBuilderHelper().endpoint(primaryFileServiceClient.getFileServiceUrl())
            .sasToken(sas)
            .buildClient();
        sc.createShare("create");
        assertDoesNotThrow(() -> sc.deleteShare("create"));
    }

    /**
     * If this test fails it means that non-deprecated string to sign has new components.
     * In that case we should hardcode version used for deprecated string to sign like we did for blobs.
     */
    @Test
    public void rememberAboutStringToSignDeprecation() {
        ShareClient client
            = shareBuilderHelper(shareName).credential(ENVIRONMENT.getPrimaryAccount().getCredential()).buildClient();
        ShareServiceSasSignatureValues values
            = new ShareServiceSasSignatureValues(testResourceNamer.now(), new ShareSasPermission());
        values.setShareName(client.getShareName());

        String deprecatedStringToSign
            = values.generateSasQueryParameters(ENVIRONMENT.getPrimaryAccount().getCredential()).encode();
        String stringToSign = client.generateSas(values);

        assertEquals(deprecatedStringToSign, stringToSign);
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void fileSasUserDelegationDelegatedObjectId() {
        liveTestScenarioWithRetry(() -> {
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);
            String sas = primaryFileClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
            // token credential.
            ShareFileClient client = instrument(
                new ShareFileClientBuilder().endpoint(primaryFileClient.getFileUrl()).sasToken(sas).credential(tokenCredential))
                .buildFileClient();

            Response<ShareFileProperties> response = client.getPropertiesWithResponse(null, Context.NONE);
            FileShareTestHelper.assertResponseStatusCode(response, 200);
        });
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void fileSasUserDelegationDelegatedObjectIdFail() {
        liveTestScenarioWithRetry(() -> {
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);
            String sas = primaryFileClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
            // token credential.
            ShareFileClient client = instrument(
                new ShareFileClientBuilder().endpoint(primaryFileClient.getFileUrl()).sasToken(sas)).buildFileClient();

            ShareStorageException e = assertThrows(ShareStorageException.class, () -> client.getPropertiesWithResponse(null, Context.NONE));
            assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED);
        });
    }


    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void shareSasUserDelegationDelegatedObjectId() {
        liveTestScenarioWithRetry(() -> {
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);
            String sas = primaryShareClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
            // token credential.
            ShareClient client = instrument(
                new ShareClientBuilder().endpoint(primaryShareClient.getShareUrl()).sasToken(sas).credential(tokenCredential))
                .buildClient();

            Response<ShareProperties> response = client.getPropertiesWithResponse(null, Context.NONE);
            FileShareTestHelper.assertResponseStatusCode(response, 200);
        });
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void shareSasUserDelegationDelegatedObjectIdFail() {
        liveTestScenarioWithRetry(() -> {
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);
            String sas = primaryShareClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
            // token credential.
            ShareClient client = instrument(
                new ShareClientBuilder().endpoint(primaryShareClient.getShareUrl()).sasToken(sas)).buildClient();

            ShareStorageException e = assertThrows(ShareStorageException.class, () -> client.getPropertiesWithResponse(null, Context.NONE));
            assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED);
        });
    }

    protected UserDelegationKey getUserDelegationInfo() {
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(testResourceNamer.now().minusDays(1),
            testResourceNamer.now().plusDays(1));
        String keyOid = testResourceNamer.recordValueFromConfig(key.getSignedOid());
        key.setSignedOid(keyOid);
        String keyTid = testResourceNamer.recordValueFromConfig(key.getSignedTid());
        key.setSignedTid(keyTid);
        return key;
    }
}
