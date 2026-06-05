// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.storagemover.models.AzureKeyVaultS3WithHmacCredentials;
import com.azure.resourcemanager.storagemover.models.AzureKeyVaultSmbCredentials;
import com.azure.resourcemanager.storagemover.models.AzureMultiCloudConnectorEndpointProperties;
import com.azure.resourcemanager.storagemover.models.AzureStorageBlobContainerEndpointProperties;
import com.azure.resourcemanager.storagemover.models.AzureStorageNfsFileShareEndpointProperties;
import com.azure.resourcemanager.storagemover.models.AzureStorageSmbFileShareEndpointProperties;
import com.azure.resourcemanager.storagemover.models.Endpoint;
import com.azure.resourcemanager.storagemover.models.EndpointKind;
import com.azure.resourcemanager.storagemover.models.EndpointType;
import com.azure.resourcemanager.storagemover.models.ManagedServiceIdentity;
import com.azure.resourcemanager.storagemover.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.storagemover.models.NfsMountEndpointProperties;
import com.azure.resourcemanager.storagemover.models.S3WithHmacEndpointProperties;
import com.azure.resourcemanager.storagemover.models.S3WithHmacSourceType;
import com.azure.resourcemanager.storagemover.models.SmbMountEndpointProperties;
import com.azure.resourcemanager.storagemover.models.SmbMountEndpointUpdateProperties;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

/**
 * Mirrors {@code EndpointTests.cs} from the .NET source-of-truth (16 methods).
 *
 * <p>Each test self-provisions its own storage mover into the per-test resource
 * group so tests stay independent. The {@code S3WithHmac} test is intentionally
 * activated (the .NET {@code [Ignore]} on it is wrong — placeholder Key Vault /
 * S3 URIs are accepted by the RP at metadata level; only running an actual data
 * job would need real credentials).
 */
public class EndpointTests extends StorageMoverManagementTestBase {

    // -----------------------------------------------------------------------
    //  Omnibus + per-type CRUD
    // -----------------------------------------------------------------------

    @Test
    public void createUpdateGetDelete() {
        String storageMoverName = createStorageMover("ep-omni");

        String cEndpointName = generateRandomResourceName("conendpoint-", 24);
        String nfsEndpointName = generateRandomResourceName("nfsendpoint-", 24);
        String smbEndpointName = generateRandomResourceName("smbendpoint-", 24);
        String fsEndpointName = generateRandomResourceName("fsendpoint-", 24);

        // Blob container endpoint.
        Endpoint cEndpoint
            = storageMoverManager.endpoints()
                .define(cEndpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new AzureStorageBlobContainerEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withBlobContainerName("testcontainer")
                    .withDescription("New container endpoint"))
                .create();
        Assertions.assertEquals(cEndpointName, cEndpoint.name());
        Assertions.assertEquals(EndpointType.AZURE_STORAGE_BLOB_CONTAINER, cEndpoint.properties().endpointType());

        Endpoint cEndpointGet = storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, cEndpointName);
        Assertions.assertEquals(cEndpointName, cEndpointGet.name());
        Assertions.assertEquals(EndpointType.AZURE_STORAGE_BLOB_CONTAINER, cEndpointGet.properties().endpointType());

        // NFS endpoint.
        Endpoint nfsEndpoint = storageMoverManager.endpoints()
            .define(nfsEndpointName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .withProperties(new NfsMountEndpointProperties().withHost("10.0.0.1")
                .withExport("/")
                .withDescription("New NFS endpoint"))
            .create();
        Assertions.assertEquals(nfsEndpointName, nfsEndpoint.name());
        Assertions.assertEquals(EndpointType.NFS_MOUNT, nfsEndpoint.properties().endpointType());
        NfsMountEndpointProperties nfsProps = (NfsMountEndpointProperties) nfsEndpoint.properties();
        Assertions.assertEquals("/", nfsProps.export());
        // Note: $..host is rewritten by the framework's default body-key
        // sanitizer (TestProxyUtils.JSON_BODY_KEYS_TO_REDACT contains "host"),
        // so playback returns the sanitised value rather than the literal IP.
        // The serialisation round-trip is covered by NfsMountEndpointPropertiesTests.
        Assertions.assertNotNull(nfsProps.host());

        // SMB endpoint with credentials.
        AzureKeyVaultSmbCredentials credentials = new AzureKeyVaultSmbCredentials()
            .withUsernameUri("https://examples-azureKeyVault.vault.azure.net/secrets/examples-username")
            .withPasswordUri("https://examples-azureKeyVault.vault.azure.net/secrets/examples-password");
        Endpoint smbEndpoint = storageMoverManager.endpoints()
            .define(smbEndpointName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .withProperties(new SmbMountEndpointProperties().withHost("10.0.0.1")
                .withShareName("testshare")
                .withCredentials(credentials)
                .withDescription("New Smb mount endpoint"))
            .create();
        SmbMountEndpointProperties smbProps = (SmbMountEndpointProperties) smbEndpoint.properties();
        Assertions.assertEquals("https://examples-azureKeyVault.vault.azure.net/secrets/examples-username",
            smbProps.credentials().usernameUri());
        Assertions.assertEquals("https://examples-azureKeyVault.vault.azure.net/secrets/examples-password",
            smbProps.credentials().passwordUri());
        // host: see comment on the NFS assertion above (sanitised in playback).
        Assertions.assertNotNull(smbProps.host());
        Assertions.assertEquals("testshare", smbProps.shareName());

        // SMB PATCH — workaround: must include identity:{type:None} at top
        // level. RP regression in api-version 2025-12-01: PATCH validation
        // requires non-null identity for SMB endpoints. See cross-language
        // playbook + Storage-XDataMove-RP/src/UserRP/Controllers/EndpointController.cs.
        AzureKeyVaultSmbCredentials clearedCredentials
            = new AzureKeyVaultSmbCredentials().withUsernameUri("").withPasswordUri("");
        SmbMountEndpointUpdateProperties updateProps
            = new SmbMountEndpointUpdateProperties().withCredentials(clearedCredentials)
                .withDescription("Update endpoint");
        Endpoint updatedSmb = smbEndpoint.update()
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
            .withProperties(updateProps)
            .apply();
        SmbMountEndpointProperties updatedSmbProps = (SmbMountEndpointProperties) updatedSmb.properties();
        Assertions.assertEquals(smbEndpointName, updatedSmb.name());
        Assertions.assertEquals(EndpointType.SMB_MOUNT, updatedSmb.properties().endpointType());
        Assertions.assertEquals("", updatedSmbProps.credentials().passwordUri());
        Assertions.assertEquals("", updatedSmbProps.credentials().usernameUri());
        // host: see comment on the NFS assertion above (sanitised in playback).
        Assertions.assertNotNull(updatedSmbProps.host());
        Assertions.assertEquals("testshare", updatedSmbProps.shareName());

        storageMoverManager.endpoints().delete(resourceGroupName, storageMoverName, smbEndpointName);

        // SMB file share endpoint.
        Endpoint fsEndpoint
            = storageMoverManager.endpoints()
                .define(fsEndpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new AzureStorageSmbFileShareEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withFileShareName("testfileshare")
                    .withDescription("new file share endpoint"))
                .create();
        AzureStorageSmbFileShareEndpointProperties fsProps
            = (AzureStorageSmbFileShareEndpointProperties) fsEndpoint.properties();
        Assertions.assertEquals(fsEndpointName, fsEndpoint.name());
        Assertions.assertEquals(EndpointType.AZURE_STORAGE_SMB_FILE_SHARE, fsEndpoint.properties().endpointType());
        Assertions.assertEquals("testfileshare", fsProps.fileShareName());
        Assertions.assertEquals("new file share endpoint", fsProps.description());

        long count = StreamSupport
            .stream(storageMoverManager.endpoints().list(resourceGroupName, storageMoverName).spliterator(), false)
            .count();
        Assertions.assertTrue(count > 1, "expected more than one endpoint but found " + count);

        // Existence assertions: positive on c+nfs+fs, negative on smb (deleted)
        // and on a never-created name.
        Assertions.assertEquals(cEndpointName,
            storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, cEndpointName).name());
        assertNotFound(
            () -> storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, cEndpointName + "111"));
        assertNotFound(() -> storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, smbEndpointName));
    }

    @Test
    public void multiCloudConnectorEndpointCreateGetDelete() {
        String storageMoverName = createStorageMover("ep-mcc");
        String endpointName = generateRandomResourceName("mcc-", 24);

        AzureMultiCloudConnectorEndpointProperties props
            = new AzureMultiCloudConnectorEndpointProperties().withMultiCloudConnectorId(MULTI_CLOUD_CONNECTOR_ID)
                .withAwsS3BucketId(AWS_S3_BUCKET_ID)
                .withDescription("Test multi-cloud connector endpoint");

        Endpoint endpoint = storageMoverManager.endpoints()
            .define(endpointName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .withProperties(props)
            .create();
        Assertions.assertEquals(endpointName, endpoint.name());
        Assertions.assertEquals(EndpointType.AZURE_MULTI_CLOUD_CONNECTOR, endpoint.properties().endpointType());

        Endpoint fetched = storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, endpointName);
        AzureMultiCloudConnectorEndpointProperties fetchedProps
            = (AzureMultiCloudConnectorEndpointProperties) fetched.properties();
        Assertions.assertEquals(endpointName, fetched.name());
        Assertions.assertEquals("Test multi-cloud connector endpoint", fetchedProps.description());
        Assertions.assertNotNull(fetchedProps.multiCloudConnectorId());
        Assertions.assertNotNull(fetchedProps.awsS3BucketId());

        storageMoverManager.endpoints().delete(resourceGroupName, storageMoverName, endpointName);
        assertNotFound(() -> storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, endpointName));
    }

    /**
     * Activated despite .NET's {@code [Ignore]}: placeholder S3 / Key Vault URIs
     * are accepted by the RP at metadata level; only running a copy job would
     * need real credentials. Verified live in the Python port.
     */
    @Test
    public void s3WithHmacEndpointCreateGetDelete() {
        String storageMoverName = createStorageMover("ep-s3");
        String endpointName = generateRandomResourceName("s3hmac-", 24);

        AzureKeyVaultS3WithHmacCredentials credentials = new AzureKeyVaultS3WithHmacCredentials()
            .withAccessKeyUri("https://examples-azureKeyVault.vault.azure.net/secrets/examples-accesskey")
            .withSecretKeyUri("https://examples-azureKeyVault.vault.azure.net/secrets/examples-secretkey");
        S3WithHmacEndpointProperties props
            = new S3WithHmacEndpointProperties().withSourceUri("https://s3.example.com/bucket")
                .withSourceType(S3WithHmacSourceType.MINIO)
                .withCredentials(credentials)
                .withDescription("Test S3 with HMAC endpoint");

        Endpoint endpoint = storageMoverManager.endpoints()
            .define(endpointName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .withProperties(props)
            .create();
        Assertions.assertEquals(endpointName, endpoint.name());
        Assertions.assertEquals(EndpointType.S3WITH_HMAC, endpoint.properties().endpointType());

        Endpoint fetched = storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, endpointName);
        S3WithHmacEndpointProperties fetchedProps = (S3WithHmacEndpointProperties) fetched.properties();
        Assertions.assertEquals(endpointName, fetched.name());
        Assertions.assertEquals("https://s3.example.com/bucket", fetchedProps.sourceUri());
        Assertions.assertEquals(S3WithHmacSourceType.MINIO, fetchedProps.sourceType());
        Assertions.assertEquals("Test S3 with HMAC endpoint", fetchedProps.description());
        Assertions.assertNotNull(fetchedProps.credentials());
        Assertions.assertEquals("https://examples-azureKeyVault.vault.azure.net/secrets/examples-accesskey",
            fetchedProps.credentials().accessKeyUri());
        Assertions.assertEquals("https://examples-azureKeyVault.vault.azure.net/secrets/examples-secretkey",
            fetchedProps.credentials().secretKeyUri());

        storageMoverManager.endpoints().delete(resourceGroupName, storageMoverName, endpointName);
        assertNotFound(() -> storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, endpointName));
    }

    @Test
    public void nfsFileShareEndpointCreateGetDelete() {
        String storageMoverName = createStorageMover("ep-nfsfs");
        String endpointName = generateRandomResourceName("nfsfs-", 24);

        AzureStorageNfsFileShareEndpointProperties props
            = new AzureStorageNfsFileShareEndpointProperties().withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                .withFileShareName("testnfsfileshare")
                .withDescription("Test NFS file share endpoint");

        Endpoint endpoint = storageMoverManager.endpoints()
            .define(endpointName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .withProperties(props)
            .create();
        Assertions.assertEquals(endpointName, endpoint.name());
        Assertions.assertEquals(EndpointType.AZURE_STORAGE_NFS_FILE_SHARE, endpoint.properties().endpointType());

        Endpoint fetched = storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, endpointName);
        AzureStorageNfsFileShareEndpointProperties fetchedProps
            = (AzureStorageNfsFileShareEndpointProperties) fetched.properties();
        Assertions.assertEquals(endpointName, fetched.name());
        Assertions.assertEquals("testnfsfileshare", fetchedProps.fileShareName());
        Assertions.assertEquals("Test NFS file share endpoint", fetchedProps.description());
        Assertions.assertNotNull(fetchedProps.storageAccountResourceId());

        storageMoverManager.endpoints().delete(resourceGroupName, storageMoverName, endpointName);
        assertNotFound(() -> storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, endpointName));
    }

    // -----------------------------------------------------------------------
    //  EndpointKind validation — valid combinations should succeed
    // -----------------------------------------------------------------------

    @Test
    public void nfsMountEndpointKindSourceSucceeds() {
        String storageMoverName = createStorageMover("ek-nfs-src");
        String endpointName = generateRandomResourceName("nfs-src-", 24);
        Endpoint endpoint = storageMoverManager.endpoints()
            .define(endpointName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .withProperties(new NfsMountEndpointProperties().withHost("10.0.0.1")
                .withExport("/")
                .withDescription("NFS source endpoint")
                .withEndpointKind(EndpointKind.SOURCE))
            .create();
        Assertions.assertEquals(EndpointKind.SOURCE, endpoint.properties().endpointKind());
    }

    @Test
    public void smbMountEndpointKindSourceSucceeds() {
        String storageMoverName = createStorageMover("ek-smb-src");
        String endpointName = generateRandomResourceName("smb-src-", 24);
        Endpoint endpoint = storageMoverManager.endpoints()
            .define(endpointName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .withProperties(new SmbMountEndpointProperties().withHost("10.0.0.1")
                .withShareName("testshare")
                .withDescription("SMB source endpoint")
                .withEndpointKind(EndpointKind.SOURCE))
            .create();
        Assertions.assertEquals(EndpointKind.SOURCE, endpoint.properties().endpointKind());
    }

    @Test
    public void multiCloudConnectorEndpointKindSourceSucceeds() {
        String storageMoverName = createStorageMover("ek-mcc-src");
        String endpointName = generateRandomResourceName("mcc-src-", 24);
        Endpoint endpoint = storageMoverManager.endpoints()
            .define(endpointName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .withProperties(
                new AzureMultiCloudConnectorEndpointProperties().withMultiCloudConnectorId(MULTI_CLOUD_CONNECTOR_ID)
                    .withAwsS3BucketId(AWS_S3_BUCKET_ID)
                    .withDescription("Multi-cloud connector source endpoint")
                    .withEndpointKind(EndpointKind.SOURCE))
            .create();
        Assertions.assertEquals(EndpointKind.SOURCE, endpoint.properties().endpointKind());
    }

    @Test
    public void blobContainerEndpointKindSourceSucceeds() {
        String storageMoverName = createStorageMover("ek-blob-src");
        String endpointName = generateRandomResourceName("blob-src-", 24);
        Endpoint endpoint
            = storageMoverManager.endpoints()
                .define(endpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new AzureStorageBlobContainerEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withBlobContainerName("testcontainer")
                    .withDescription("Blob container source endpoint")
                    .withEndpointKind(EndpointKind.SOURCE))
                .create();
        Assertions.assertEquals(EndpointKind.SOURCE, endpoint.properties().endpointKind());
    }

    @Test
    public void blobContainerEndpointKindTargetSucceeds() {
        String storageMoverName = createStorageMover("ek-blob-tgt");
        String endpointName = generateRandomResourceName("blob-tgt-", 24);
        Endpoint endpoint
            = storageMoverManager.endpoints()
                .define(endpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new AzureStorageBlobContainerEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withBlobContainerName("testcontainer")
                    .withDescription("Blob container target endpoint")
                    .withEndpointKind(EndpointKind.TARGET))
                .create();
        Assertions.assertEquals(EndpointKind.TARGET, endpoint.properties().endpointKind());
    }

    @Test
    public void smbFileShareEndpointKindTargetSucceeds() {
        String storageMoverName = createStorageMover("ek-smbfs-tgt");
        String endpointName = generateRandomResourceName("smbfs-tgt-", 24);
        Endpoint endpoint
            = storageMoverManager.endpoints()
                .define(endpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new AzureStorageSmbFileShareEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withFileShareName("testfileshare")
                    .withDescription("SMB file share target endpoint")
                    .withEndpointKind(EndpointKind.TARGET))
                .create();
        Assertions.assertEquals(EndpointKind.TARGET, endpoint.properties().endpointKind());
    }

    @Test
    public void nfsFileShareEndpointKindTargetSucceeds() {
        String storageMoverName = createStorageMover("ek-nfsfs-tgt");
        String endpointName = generateRandomResourceName("nfsfs-tgt-", 24);
        Endpoint endpoint
            = storageMoverManager.endpoints()
                .define(endpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new AzureStorageNfsFileShareEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withFileShareName("testnfsfileshare")
                    .withDescription("NFS file share target endpoint")
                    .withEndpointKind(EndpointKind.TARGET))
                .create();
        Assertions.assertEquals(EndpointKind.TARGET, endpoint.properties().endpointKind());
    }

    // -----------------------------------------------------------------------
    //  EndpointKind validation — invalid combinations must fail
    // -----------------------------------------------------------------------

    @Test
    public void nfsMountEndpointKindTargetFails() {
        String storageMoverName = createStorageMover("ek-nfs-tgt-fail");
        String endpointName = generateRandomResourceName("nfs-tgt-", 24);
        Assertions.assertThrows(ManagementException.class,
            () -> storageMoverManager.endpoints()
                .define(endpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new NfsMountEndpointProperties().withHost("10.0.0.1")
                    .withExport("/")
                    .withEndpointKind(EndpointKind.TARGET))
                .create());
    }

    @Test
    public void smbMountEndpointKindTargetFails() {
        String storageMoverName = createStorageMover("ek-smb-tgt-fail");
        String endpointName = generateRandomResourceName("smb-tgt-", 24);
        Assertions.assertThrows(ManagementException.class,
            () -> storageMoverManager.endpoints()
                .define(endpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new SmbMountEndpointProperties().withHost("10.0.0.1")
                    .withShareName("testshare")
                    .withEndpointKind(EndpointKind.TARGET))
                .create());
    }

    @Test
    public void multiCloudConnectorEndpointKindTargetFails() {
        String storageMoverName = createStorageMover("ek-mcc-tgt-fail");
        String endpointName = generateRandomResourceName("mcc-tgt-", 24);
        Assertions
            .assertThrows(ManagementException.class,
                () -> storageMoverManager.endpoints()
                    .define(endpointName)
                    .withExistingStorageMover(resourceGroupName, storageMoverName)
                    .withProperties(new AzureMultiCloudConnectorEndpointProperties()
                        .withMultiCloudConnectorId(MULTI_CLOUD_CONNECTOR_ID)
                        .withAwsS3BucketId(AWS_S3_BUCKET_ID)
                        .withEndpointKind(EndpointKind.TARGET))
                    .create());
    }

    @Test
    public void smbFileShareEndpointKindSourceFails() {
        String storageMoverName = createStorageMover("ek-smbfs-src-fail");
        String endpointName = generateRandomResourceName("smbfs-src-", 24);
        Assertions.assertThrows(ManagementException.class,
            () -> storageMoverManager.endpoints()
                .define(endpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new AzureStorageSmbFileShareEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withFileShareName("testfileshare")
                    .withEndpointKind(EndpointKind.SOURCE))
                .create());
    }

    @Test
    public void nfsFileShareEndpointKindSourceFails() {
        String storageMoverName = createStorageMover("ek-nfsfs-src-fail");
        String endpointName = generateRandomResourceName("nfsfs-src-", 24);
        Assertions.assertThrows(ManagementException.class,
            () -> storageMoverManager.endpoints()
                .define(endpointName)
                .withExistingStorageMover(resourceGroupName, storageMoverName)
                .withProperties(new AzureStorageNfsFileShareEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withFileShareName("testnfsfileshare")
                    .withEndpointKind(EndpointKind.SOURCE))
                .create());
    }

    // -----------------------------------------------------------------------
    //  Helpers
    // -----------------------------------------------------------------------

    private String createStorageMover(String prefix) {
        StorageMover sm = storageMoverManager.storageMovers()
            .define(generateRandomResourceName("sm-" + prefix + "-", 24))
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();
        return sm.name();
    }
}
