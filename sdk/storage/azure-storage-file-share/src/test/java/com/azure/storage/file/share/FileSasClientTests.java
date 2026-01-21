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
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareInfo;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.models.UserDelegationKey;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static com.azure.storage.common.test.shared.StorageCommonTestUtils.getOidFromToken;
import static com.azure.storage.file.share.FileShareTestHelper.assertExceptionStatusCodeAndMessage;
import static com.azure.storage.file.share.FileShareTestHelper.assertResponseStatusCode;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);
            String sas = primaryFileClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
            // token credential.
            ShareFileClient client = instrument(new ShareFileClientBuilder().endpoint(primaryFileClient.getFileUrl())
                .sasToken(sas)
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .credential(tokenCredential)).buildFileClient();

            Response<ShareFileProperties> response = client.getPropertiesWithResponse(null, Context.NONE);
            FileShareTestHelper.assertResponseStatusCode(response, 200);
        });
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void fileSasUserDelegationDelegatedObjectIdAsync() {
        liveTestScenarioWithRetry(() -> {
            ShareFileAsyncClient primaryFileAsyncClient = fileBuilderHelper(shareName, filePath).buildFileAsyncClient();
            ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);

            Flux<Response<ShareFileProperties>> response = getUserDelegationInfoAsync().flatMapMany(key -> {
                String sas = primaryFileAsyncClient.generateUserDelegationSas(sasValues, key);
                // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
                // token credential.
                ShareFileAsyncClient client
                    = instrument(new ShareFileClientBuilder().endpoint(primaryFileClient.getFileUrl())
                        .sasToken(sas)
                        .shareTokenIntent(ShareTokenIntent.BACKUP)
                        .credential(tokenCredential)).buildFileAsyncClient();

                return client.getPropertiesWithResponse();
            });

            StepVerifier.create(response).assertNext(r -> assertResponseStatusCode(r, 200)).verifyComplete();
        });
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void fileSasUserDelegationDelegatedObjectIdFail() {
        liveTestScenarioWithRetry(() -> {
            ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);
            String sas = primaryFileClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
            // token credential. No token credential here.
            ShareFileClient client = instrument(new ShareFileClientBuilder().endpoint(primaryFileClient.getFileUrl())
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .sasToken(sas)).buildFileClient();

            ShareStorageException e
                = assertThrows(ShareStorageException.class, () -> client.getPropertiesWithResponse(null, Context.NONE));
            assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED);
        });
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void fileSasUserDelegationDelegatedObjectIdFailAsync() {
        liveTestScenarioWithRetry(() -> {
            ShareFileAsyncClient primaryFileAsyncClient = fileBuilderHelper(shareName, filePath).buildFileAsyncClient();
            ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);

            Flux<Response<ShareFileProperties>> response = getUserDelegationInfoAsync().flatMapMany(key -> {
                String sas = primaryFileAsyncClient.generateUserDelegationSas(sasValues, key);
                // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
                // token credential. No token credential here.
                ShareFileAsyncClient client
                    = instrument(new ShareFileClientBuilder().endpoint(primaryFileClient.getFileUrl())
                        .sasToken(sas)
                        .shareTokenIntent(ShareTokenIntent.BACKUP)).buildFileAsyncClient();

                return client.getPropertiesWithResponse();
            });

            StepVerifier.create(response)
                .verifyErrorSatisfies(
                    e -> assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED));
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
            ShareClient client = instrument(new ShareClientBuilder().endpoint(primaryShareClient.getShareUrl())
                .sasToken(sas)
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .credential(tokenCredential)).buildClient();

            Response<ShareProperties> response = client.getPropertiesWithResponse(null, Context.NONE);
            FileShareTestHelper.assertResponseStatusCode(response, 200);
        });
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void shareSasUserDelegationDelegatedObjectIdAsync() {
        liveTestScenarioWithRetry(() -> {
            ShareAsyncClient primaryShareAsyncClient = shareBuilderHelper(shareName).buildAsyncClient();
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);

            Flux<Response<ShareProperties>> response = getUserDelegationInfoAsync().flatMapMany(key -> {
                String sas = primaryShareAsyncClient.generateUserDelegationSas(sasValues, key);
                // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
                // token credential.
                ShareAsyncClient client
                    = instrument(new ShareClientBuilder().endpoint(primaryShareAsyncClient.getShareUrl())
                        .sasToken(sas)
                        .shareTokenIntent(ShareTokenIntent.BACKUP)
                        .credential(tokenCredential)).buildAsyncClient();

                return client.getPropertiesWithResponse();
            });

            StepVerifier.create(response).assertNext(r -> assertResponseStatusCode(r, 200)).verifyComplete();
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
            // token credential. No token credential here.
            ShareClient client = instrument(new ShareClientBuilder().endpoint(primaryShareClient.getShareUrl())
                .sasToken(sas)
                .shareTokenIntent(ShareTokenIntent.BACKUP)).buildClient();

            ShareStorageException e
                = assertThrows(ShareStorageException.class, () -> client.getPropertiesWithResponse(null, Context.NONE));
            assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED);
        });
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void shareSasUserDelegationDelegatedObjectIdFailAsync() {
        liveTestScenarioWithRetry(() -> {
            ShareAsyncClient primaryShareAsyncClient = shareBuilderHelper(shareName).buildAsyncClient();
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            ShareServiceSasSignatureValues sasValues
                = new ShareServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);

            Flux<Response<ShareProperties>> response = getUserDelegationInfoAsync().flatMapMany(key -> {
                String sas = primaryShareAsyncClient.generateUserDelegationSas(sasValues, key);
                // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
                // token credential. No token credential here.
                ShareAsyncClient client
                    = instrument(new ShareClientBuilder().endpoint(primaryShareAsyncClient.getShareUrl())
                        .sasToken(sas)
                        .shareTokenIntent(ShareTokenIntent.BACKUP)).buildAsyncClient();

                return client.getPropertiesWithResponse();
            });

            StepVerifier.create(response)
                .verifyErrorSatisfies(
                    e -> assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED));
        });
    }

    protected UserDelegationKey getUserDelegationInfo() {
        UserDelegationKey key = getOAuthServiceClient().getUserDelegationKey(testResourceNamer.now().minusDays(1),
            testResourceNamer.now().plusDays(1));
        String keyOid = testResourceNamer.recordValueFromConfig(key.getSignedObjectId());
        key.setSignedObjectId(keyOid);
        String keyTid = testResourceNamer.recordValueFromConfig(key.getSignedTenantId());
        key.setSignedTenantId(keyTid);
        return key;
    }

    private Mono<UserDelegationKey> getUserDelegationInfoAsync() {
        return getOAuthServiceAsyncClient()
            .getUserDelegationKey(testResourceNamer.now().minusDays(1), testResourceNamer.now().plusDays(1))
            .flatMap(r -> {
                String keyOid = testResourceNamer.recordValueFromConfig(r.getSignedObjectId());
                r.setSignedObjectId(keyOid);
                String keyTid = testResourceNamer.recordValueFromConfig(r.getSignedTenantId());
                r.setSignedTenantId(keyTid);
                return Mono.just(r);
            });
    }

    // RBAC replication lag
    // Make sure a file SAS with user delegation key CANNOT be used to access other files in the share
    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void verifyScopeFileSasUserDelegation() {
        liveTestScenarioWithRetry(() -> {
            ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true);

            ShareServiceSasSignatureValues sasValues = generateValues(permissions);

            ShareFileClient client1 = primaryShareClient.createFile(generatePathName(), Constants.KB);
            ShareFileClient client2 = primaryShareClient.createFile(generatePathName(), Constants.KB);
            String client1Sas = client1.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            ShareFileClient correctClientAuth
                = fileBuilderHelper(shareName, client1.getFilePath()).endpoint(client1.getFileUrl())
                    .sasToken(client1Sas)
                    .shareTokenIntent(ShareTokenIntent.BACKUP)
                    .buildFileClient();

            assertDoesNotThrow(
                () -> correctClientAuth.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()));

            ShareFileClient wrongClientAuth
                = fileBuilderHelper(shareName, client2.getFilePath()).endpoint(client2.getFileUrl())
                    .sasToken(client1Sas)
                    .shareTokenIntent(ShareTokenIntent.BACKUP)
                    .buildFileClient();

            assertThrows(ShareStorageException.class,
                () -> wrongClientAuth.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()));
        });
    }

    // RBAC replication lag
    // Make sure a file SAS with user delegation key CANNOT be used to access other files in the share
    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void verifyScopeAsyncFileSasUserDelegation() {
        liveTestScenarioWithRetry(() -> {
            ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true);

            ShareServiceSasSignatureValues sasValues = generateValues(permissions);

            ShareAsyncClient shareAsyncClient = shareBuilderHelper(shareName).buildAsyncClient();
            ShareFileAsyncClient client1 = shareAsyncClient.getFileClient(generatePathName());
            ShareFileAsyncClient client2 = shareAsyncClient.getFileClient(generatePathName());

            String sas = client1.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            Mono<ShareFileInfo> createMono = client1.create(Constants.KB).then(client2.create(Constants.KB));

            ShareFileAsyncClient correctClientAuth
                = fileBuilderHelper(shareName, client1.getFilePath()).endpoint(client1.getFileUrl())
                    .sasToken(sas)
                    .shareTokenIntent(ShareTokenIntent.BACKUP)
                    .buildFileAsyncClient();

            ShareFileAsyncClient wrongClientAuth
                = fileBuilderHelper(shareName, client2.getFilePath()).endpoint(client2.getFileUrl())
                    .sasToken(sas)
                    .shareTokenIntent(ShareTokenIntent.BACKUP)
                    .buildFileAsyncClient();

            Mono<ShareFileUploadInfo> correctClientAuthUpload
                = correctClientAuth.uploadRange(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong());
            Mono<ShareFileUploadInfo> wrongClientAuthUpload
                = wrongClientAuth.uploadRange(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong());

            StepVerifier.create(createMono.then(correctClientAuthUpload)).expectNextCount(1).verifyComplete();

            StepVerifier.create(wrongClientAuthUpload).verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertEquals(ShareErrorCode.AUTHENTICATION_FAILED, e.getErrorCode());
            });

        });
    }

    // RBAC replication lag
    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void fileSasUserDelegation() {
        liveTestScenarioWithRetry(() -> {
            ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true);

            ShareServiceSasSignatureValues sasValues = generateValues(permissions);

            String sas = primaryFileClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            ShareFileClient client = fileBuilderHelper(shareName, filePath).endpoint(primaryFileClient.getFileUrl())
                .sasToken(sas)
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .buildFileClient();

            client.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            client.downloadWithResponse(stream, new ShareFileRange(0L, DATA.getDefaultDataSizeLong() - 1), null, null,
                Context.NONE);

            ShareFileProperties properties = client.getProperties();

            assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
            assertTrue(validateSasProperties(properties));
        });
    }

    // RBAC replication lag
    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void fileSasUserDelegationAsync() {
        liveTestScenarioWithRetry(() -> {
            ShareFileAsyncClient primaryFileAsyncClient = fileBuilderHelper(shareName, filePath).buildFileAsyncClient();
            ShareFileSasPermission permissions = new ShareFileSasPermission().setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true);

            ShareServiceSasSignatureValues sasValues = generateValues(permissions);

            Flux<Response<ShareFileProperties>> response = getUserDelegationInfoAsync().flatMapMany(key -> {
                String sas = primaryFileAsyncClient.generateUserDelegationSas(sasValues, key);
                ShareFileAsyncClient client
                    = instrument(new ShareFileClientBuilder().endpoint(primaryFileAsyncClient.getFileUrl())
                        .sasToken(sas)
                        .shareTokenIntent(ShareTokenIntent.BACKUP)).buildFileAsyncClient();

                return client.getPropertiesWithResponse();
            });

            StepVerifier.create(response).assertNext(r -> assertResponseStatusCode(r, 200)).verifyComplete();
        });
    }

    // RBAC replication lag
    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void shareSasUserDelegation() {
        liveTestScenarioWithRetry(() -> {
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true);

            ShareServiceSasSignatureValues sasValues = generateValues(permissions);

            String sas = primaryShareClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            ShareClient client = shareBuilderHelper(shareName).endpoint(primaryShareClient.getShareUrl())
                .sasToken(sas)
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .buildClient();

            FileShareTestHelper.assertResponseStatusCode(
                client.createDirectoryWithResponse(generatePathName(), null, null, null, null, Context.NONE), 201);
        });
    }

    // RBAC replication lag
    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void shareSasUserDelegationAsync() {
        liveTestScenarioWithRetry(() -> {
            ShareAsyncClient primaryAsyncClient = shareBuilderHelper(shareName).buildAsyncClient();
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true);

            ShareServiceSasSignatureValues sasValues = generateValues(permissions);

            Flux<Response<ShareDirectoryAsyncClient>> response = getUserDelegationInfoAsync().flatMapMany(key -> {
                String sas = primaryAsyncClient.generateUserDelegationSas(sasValues, key);
                ShareAsyncClient client = instrument(new ShareClientBuilder().endpoint(primaryAsyncClient.getShareUrl())
                    .sasToken(sas)
                    .shareTokenIntent(ShareTokenIntent.BACKUP)).buildAsyncClient();

                return client.createDirectoryWithResponse(generatePathName(), null, null, null);
            });

            StepVerifier.create(response).assertNext(r -> assertResponseStatusCode(r, 201)).verifyComplete();
        });
    }

    // RBAC replication lag
    // Make sure a file SAS with user delegation key CANNOT be used to access other files in the share
    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void verifyScopeShareSasUserDelegation() {
        liveTestScenarioWithRetry(() -> {
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true);

            ShareServiceSasSignatureValues sasValues = generateValues(permissions);

            ShareClient client1 = primaryFileServiceClient.createShare(generateShareName());
            ShareClient client2 = primaryFileServiceClient.createShare(generateShareName());
            String client1Sas = client1.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            ShareClient correctClientAuth = shareBuilderHelper(shareName, null).endpoint(client1.getShareUrl())
                .sasToken(client1Sas)
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .buildClient();

            assertDoesNotThrow(() -> correctClientAuth.createFile(generatePathName(), Constants.KB));

            ShareClient wrongClientAuth = shareBuilderHelper(shareName, null).endpoint(client2.getShareUrl())
                .sasToken(client1Sas)
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .buildClient();

            assertThrows(ShareStorageException.class,
                () -> wrongClientAuth.createFile(generatePathName(), Constants.KB));
        });
    }

    // RBAC replication lag
    // Make sure a file SAS with user delegation key CANNOT be used to access other files in the share
    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2026-02-06")
    public void verifyScopeAsyncShareSasUserDelegation() {
        liveTestScenarioWithRetry(() -> {
            ShareSasPermission permissions = new ShareSasPermission().setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true);

            ShareServiceSasSignatureValues sasValues = generateValues(permissions);

            ShareServiceAsyncClient shareServiceAsyncClient = fileServiceBuilderHelper().buildAsyncClient();
            ShareAsyncClient client1 = shareServiceAsyncClient.getShareAsyncClient(generateShareName());
            ShareAsyncClient client2 = shareServiceAsyncClient.getShareAsyncClient(generateShareName());

            String sas = client1.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            Mono<ShareInfo> createMono = client1.create().then(client2.create());

            ShareAsyncClient correctClientAuth = shareBuilderHelper(shareName, null).endpoint(client1.getShareUrl())
                .sasToken(sas)
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .buildAsyncClient();

            ShareAsyncClient wrongClientAuth = shareBuilderHelper(shareName, null).endpoint(client2.getShareUrl())
                .sasToken(sas)
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .buildAsyncClient();

            Mono<ShareFileAsyncClient> correctClientAuthUpload
                = correctClientAuth.createFile(generatePathName(), Constants.KB);
            Mono<ShareFileAsyncClient> wrongClientAuthUpload
                = wrongClientAuth.createFile(generatePathName(), Constants.KB);

            StepVerifier.create(createMono.then(correctClientAuthUpload)).expectNextCount(1).verifyComplete();

            StepVerifier.create(wrongClientAuthUpload).verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertEquals(ShareErrorCode.AUTHENTICATION_FAILED, e.getErrorCode());
            });

        });
    }

    private boolean validateSasProperties(ShareFileProperties properties) {
        boolean ret;
        ret = properties.getCacheControl().equals("cache");
        ret &= properties.getContentDisposition().equals("disposition");
        ret &= properties.getContentEncoding().equals("encoding");
        return ret;
    }

    ShareServiceSasSignatureValues generateValues(ShareSasPermission permission) {
        return new ShareServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setCacheControl("cache")
            .setContentDisposition("disposition")
            .setContentEncoding("encoding")
            .setContentLanguage("language")
            .setContentType("type");
    }
}
