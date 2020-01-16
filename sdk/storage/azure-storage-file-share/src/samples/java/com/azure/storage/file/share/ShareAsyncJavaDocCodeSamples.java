// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.models.ShareAccessPolicy;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ShareAsyncClient}.
 */
public class ShareAsyncJavaDocCodeSamples {


    /**
     * Generates code sample for {@link ShareAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.instantiation
        ShareAsyncClient client = new ShareClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildAsyncClient();
        // END: com.azure.storage.file.share.ShareAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link ShareAsyncClient} with SAS token.
     *
     * @return An instance of {@link ShareAsyncClient}
     */
    public ShareAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.instantiation.sastoken
        ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .shareName("myshare")
            .buildAsyncClient();
        // END: com.azure.storage.file.share.ShareAsyncClient.instantiation.sastoken
        return shareAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareAsyncClient} with SAS token.
     *
     * @return An instance of {@link ShareAsyncClient}
     */
    public ShareAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.instantiation.credential
        ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .buildAsyncClient();
        // END: com.azure.storage.file.share.ShareAsyncClient.instantiation.credential
        return shareAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareAsyncClient} with {@code connectionString} which turns into
     * {@link StorageSharedKeyCredential}
     *
     * @return An instance of {@link ShareAsyncClient}
     */
    public ShareAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
            .connectionString(connectionString).shareName("myshare")
            .buildAsyncClient();
        // END: com.azure.storage.file.share.ShareAsyncClient.instantiation.connectionstring
        return shareAsyncClient;
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#create}
     */
    public void createShareAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.create
        shareAsyncClient.create().subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createWithResponse(Map, Integer)} with Metadata.
     */
    public void createWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.metadata
        shareAsyncClient.createWithResponse(Collections.singletonMap("share", "metadata"), null).subscribe(
            response -> System.out.printf("Creating the share completed with status code %d", response.getStatusCode()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createWithResponse(Map, Integer)} with Quota.
     */
    public void createShareAsyncMaxOverloadQuota() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.quota
        shareAsyncClient.createWithResponse(null, 10).subscribe(
            response -> System.out.printf("Creating the share completed with status code %d", response.getStatusCode()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.createWithResponse#map-integer.quota
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createDirectory(String)}
     */
    public void createDirectoryAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createDirectory#string
        shareAsyncClient.createDirectory("mydirectory").subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the directory!")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.createDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createFile(String, long)}
     */
    public void createFileAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createFile#string-long
        shareAsyncClient.createFile("myfile", 1024).subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the directory!")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createSnapshot()}
     */
    public void createSnapshotAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createSnapshot
        shareAsyncClient.createSnapshot().subscribe(
            response -> System.out.println("Successfully creating the share snapshot with snapshot id: "
                + response.getSnapshot()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete creating the share snapshot.")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.createSnapshot
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createSnapshotWithResponse(Map)}
     */
    public void createSnapshotAsyncWithMetadata() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createSnapshotWithResponse#map
        shareAsyncClient.createSnapshotWithResponse(Collections.singletonMap("snapshot", "metadata")).subscribe(
            response -> System.out.println("Successfully creating the share snapshot with snapshot id: "
                + response.getValue().getSnapshot()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete creating the share snapshot.")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.createSnapshotWithResponse#map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createDirectoryWithResponse(String, FileSmbProperties,
     * String, Map)}
     */
    public void createDirectoryWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createDirectoryWithResponse#String-FileSmbProperties-String-Map
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        shareAsyncClient.createDirectoryWithResponse("documents", smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"))
            .subscribe(response -> System.out.printf("Creating the directory completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareAsyncClient.createDirectoryWithResponse#String-FileSmbProperties-String-Map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createFile(String, long)}
     */
    public void createFileAsyncMaxOverload() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createFile#string-long-filehttpheaders-map
        shareAsyncClient.createFile("myfile", 1024)
            .doOnSuccess(response -> System.out.println("Creating the file completed."));
        // END: com.azure.storage.file.share.ShareAsyncClient.createFile#string-long-filehttpheaders-map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createFileWithResponse(String, long, ShareFileHttpHeaders,
     * FileSmbProperties, String, Map)}
     */
    public void createFileWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        shareAsyncClient.createFileWithResponse("myfile", 1024, httpHeaders, smbProperties,
            filePermission, Collections.singletonMap("directory", "metadata"))
            .subscribe(response -> System.out.printf("Creating the file completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#deleteDirectory(String)}
     */
    public void deleteDirectoryAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.deleteDirectory#string
        shareAsyncClient.deleteDirectory("mydirectory").subscribe(
            response -> {
            },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the directory.")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.deleteDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#deleteFile(String)}
     */
    public void deleteFileAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.deleteFile#string
        shareAsyncClient.deleteFile("myfile").subscribe(
            response -> {
            },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the file.")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#delete}
     */
    public void deleteShareAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.delete
        shareAsyncClient.delete().subscribe(
            response -> System.out.println("Deleting the shareAsyncClient completed."),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the share.")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#deleteWithResponse()}
     */
    public void deleteWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.deleteWithResponse
        shareAsyncClient.deleteWithResponse().subscribe(
            response -> System.out.println("Deleting the shareAsyncClient completed with status code: "
                + response.getStatusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the share.")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.deleteWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.getProperties
        shareAsyncClient.getProperties()
            .subscribe(properties -> {
                System.out.printf("Share quota: %d, Metadata: %s", properties.getQuota(), properties.getMetadata());
            });
        // END: com.azure.storage.file.share.ShareAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.getPropertiesWithResponse
        shareAsyncClient.getPropertiesWithResponse()
            .subscribe(properties -> {
                System.out.printf("Share quota: %d, Metadata: %s", properties.getValue().getQuota(),
                    properties.getValue().getMetadata());
            });
        // END: com.azure.storage.file.share.ShareAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setQuota(int)}
     */
    public void setQuotaAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.setQuota#int
        shareAsyncClient.setQuota(1024).doOnSuccess(response ->
            System.out.println("Setting the share quota completed.")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.setQuota#int
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setQuotaWithResponse(int)}
     */
    public void setQuotaWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.setQuotaWithResponse#int
        shareAsyncClient.setQuotaWithResponse(1024)
            .subscribe(response ->
                System.out.printf("Setting the share quota completed with status code %d", response.getStatusCode())
            );
        // END: com.azure.storage.file.share.ShareAsyncClient.setQuotaWithResponse#int
    }


    /**
     * Generates a code sample for using {@link ShareAsyncClient#setMetadata(Map)}
     */
    public void setMetadataAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.setMetadata#map
        shareAsyncClient.setMetadata(Collections.singletonMap("share", "updatedMetadata")).doOnSuccess(response ->
            System.out.println("Setting the share metadata completed.")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setMetadataWithResponse(Map)}
     */
    public void setMetadataWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.setMetadataWithResponse#map
        shareAsyncClient.setMetadataWithResponse(Collections.singletonMap("share", "updatedMetadata"))
            .subscribe(response ->
                System.out.printf("Setting the share metadata completed with status code %d", response.getStatusCode())
            );
        // END: com.azure.storage.file.share.ShareAsyncClient.setMetadataWithResponse#map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadataAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.clearMetadata#map
        shareAsyncClient.setMetadata(null).doOnSuccess(response ->
            System.out.println("Setting the share metadata completed.")
        );
        // END: com.azure.storage.file.share.ShareAsyncClient.clearMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicyAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.getAccessPolicy
        shareAsyncClient.getAccessPolicy()
            .subscribe(result -> System.out.printf("Access policy %s allows these permissions: %s", result.getId(),
                result.getAccessPolicy().getPermissions())
            );
        // END: com.azure.storage.file.share.ShareAsyncClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setAccessPolicy(List)}
     */
    public void setAccessPolicyAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.setAccessPolicy#List
        ShareAccessPolicy accessPolicy = new ShareAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        ShareSignedIdentifier permission = new ShareSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        shareAsyncClient.setAccessPolicy(Collections.singletonList(permission)).doOnSuccess(
            response -> System.out.println("Setting access policies completed."));
        // END: com.azure.storage.file.share.ShareAsyncClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setAccessPolicyWithResponse(List)}
     */
    public void setAccessPolicyWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.setAccessPolicyWithResponse#List
        ShareAccessPolicy accessPolicy = new ShareAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        ShareSignedIdentifier permission = new ShareSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        shareAsyncClient.setAccessPolicyWithResponse(Collections.singletonList(permission))
            .subscribe(response -> System.out.printf("Setting access policies completed completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareAsyncClient.setAccessPolicyWithResponse#List
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getStatistics()}
     */
    public void getStatisticsAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.getStatistics
        shareAsyncClient.getStatistics().doOnSuccess(response -> System.out.printf("The share is using %d GB",
            response.getShareUsageInGB()));
        // END: com.azure.storage.file.share.ShareAsyncClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createPermission(String)}
     */
    public void createPermissionAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createPermission#string
        shareAsyncClient.createPermission("filePermission").subscribe(
            response -> System.out.printf("The file permission key is %s", response));
        // END: com.azure.storage.file.share.ShareAsyncClient.createPermission#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createPermissionWithResponse(String)}
     */
    public void createPermissionWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.createPermissionWithResponse#string
        shareAsyncClient.createPermissionWithResponse("filePermission").subscribe(
            response -> System.out.printf("The file permission key is %s", response.getValue()));
        // END: com.azure.storage.file.share.ShareAsyncClient.createPermissionWithResponse#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getPermission(String)}
     */
    public void getPermissionAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.getPermission#string
        shareAsyncClient.getPermission("filePermissionKey").subscribe(
            response -> System.out.printf("The file permission is %s", response));
        // END: com.azure.storage.file.share.ShareAsyncClient.getPermission#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getPermissionWithResponse(String)}
     */
    public void getPermissionWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.getPermissionWithResponse#string
        shareAsyncClient.getPermissionWithResponse("filePermissionKey").subscribe(
            response -> System.out.printf("The file permission is %s", response.getValue()));
        // END: com.azure.storage.file.share.ShareAsyncClient.getPermissionWithResponse#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getStatisticsWithResponse()}
     */
    public void getStatisticsWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.getStatisticsWithResponse
        shareAsyncClient.getStatisticsWithResponse().subscribe(response -> System.out.printf("The share is using %d GB",
            response.getValue().getShareUsageInGB()));
        // END: com.azure.storage.file.share.ShareAsyncClient.getStatisticsWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getSnapshotId()}
     */
    public void getSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.getSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .snapshot(currentTime.toString())
            .buildAsyncClient();

        System.out.printf("Snapshot ID: %s%n", shareAsyncClient.getSnapshotId());
        // END: com.azure.storage.file.share.ShareAsyncClient.getSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getShareName()}
     */
    public void getShareNameAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.getShareName
        String shareName = shareAsyncClient.getShareName();
        System.out.println("The name of the share is " + shareName);
        // END: com.azure.storage.file.share.ShareAsyncClient.getShareName
    }

    /**
     * Code snippet for {@link ShareAsyncClient#generateSas(ShareServiceSasSignatureValues)}
     */
    public void generateSas() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareAsyncClient.generateSas#ShareServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        ShareSasPermission permission = new ShareSasPermission().setReadPermission(true);

        ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        shareAsyncClient.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.file.share.ShareAsyncClient.generateSas#ShareServiceSasSignatureValues
    }
}
