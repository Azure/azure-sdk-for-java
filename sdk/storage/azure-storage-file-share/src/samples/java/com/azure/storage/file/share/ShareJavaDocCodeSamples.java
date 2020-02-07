// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.models.ShareAccessPolicy;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareInfo;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareStatistics;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ShareClient}.
 */
public class ShareJavaDocCodeSamples {

    private String key1 = "key1";
    private String value1 = "val1";

    /**
     * Generates code sample for {@link ShareClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.share.ShareClient.instantiation
        ShareClient client = new ShareClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildClient();
        // END: com.azure.storage.file.share.ShareClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link ShareClient} with SAS token
     * @return An instance of {@link ShareClient}
     */
    public ShareClient createClientWithSASToken() {

        // BEGIN: com.azure.storage.file.share.ShareClient.instantiation.sastoken
        ShareClient shareClient = new ShareClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .buildClient();
        // END: com.azure.storage.file.share.ShareClient.instantiation.sastoken
        return shareClient;
    }


    /**
     * Generates code sample for creating a {@link ShareClient} with SAS token
     * @return An instance of {@link ShareClient}
     */
    public ShareClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.share.ShareClient.instantiation.credential
        ShareClient shareClient = new ShareClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .buildClient();
        // END: com.azure.storage.file.share.ShareClient.instantiation.credential
        return shareClient;
    }

    /**
     * Generates code sample for creating a {@link ShareClient} with {@code connectionString} which turns into {@link StorageSharedKeyCredential}
     * @return An instance of {@link ShareClient}
     */
    public ShareClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.share.ShareClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        ShareClient shareClient = new ShareClientBuilder()
            .connectionString(connectionString).shareName("myshare")
            .buildClient();
        // END: com.azure.storage.file.share.ShareClient.instantiation.connectionstring
        return shareClient;
    }

    /**
     * Generates a code sample for using {@link ShareClient#create()}
     */
    public void createShare() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.create
        ShareInfo response = shareClient.create();
        System.out.println("Complete creating the shares with status code: " + response);
        // END: com.azure.storage.file.share.ShareClient.create
    }

    /**
     * Generates a code sample for using {@link ShareClient#create()}.
     */
    public void createShareMaxOverloadMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.create#map-integer.metadata
        ShareInfo response = shareClient.create();
        System.out.println("Complete creating the shares.");
        // END: com.azure.storage.file.share.ShareClient.create#map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link ShareClient#createWithResponse(Map, Integer,
     * Duration, Context)} with Quota.
     */
    public void createWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: ShareClient.createWithResponse#map-integer-duration-context.quota
        Response<ShareInfo> response = shareClient.createWithResponse(null, 10,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete creating the shares with status code: " + response.getStatusCode());
        // END: ShareClient.createWithResponse#map-integer-duration-context.quota
    }

    /**
     * Generates a code sample for using {@link ShareClient#createWithResponse(Map, Integer,
     * Duration, Context)} with Metadata.
     */
    public void createWithResponseMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: ShareClient.createWithResponse#map-integer-duration-context.metadata
        Response<ShareInfo> response = shareClient.createWithResponse(Collections.singletonMap("share", "metadata"),
            null, Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete creating the shares with status code: " + response.getStatusCode());
        // END: ShareClient.createWithResponse#map-integer-duration-context.metadata
    }

    /**
     * Generates a code sample for using {@link ShareClient#createDirectory(String)} ()}
     */
    public void createDirectory() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.createDirectory#string
        ShareDirectoryClient response = shareClient.createDirectory("mydirectory");
        System.out.println("Complete creating the directory.");
        // END: com.azure.storage.file.share.ShareClient.createDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#createDirectoryWithResponse(String, FileSmbProperties, String, Map, Duration, Context)}
     */
    public void createDirectoryWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.createDirectoryWithResponse#String-FileSmbProperties-String-Map-Duration-Context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<ShareDirectoryClient> response = shareClient.createDirectoryWithResponse("documents",
            smbProperties, filePermission, Collections.singletonMap("directory", "metadata"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Creating the directory completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareClient.createDirectoryWithResponse#String-FileSmbProperties-String-Map-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareClient#createFile(String, long)}
     */
    public void createFile() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.createFile#string-long
        ShareFileClient response = shareClient.createFile("myfile", 1024);
        System.out.println("Complete creating the file with snapshot Id:" + response.getShareSnapshotId());
        // END: com.azure.storage.file.share.ShareClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link ShareClient#createSnapshot()}
     */
    public void createSnapshot() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.createSnapshot
        ShareSnapshotInfo response = shareClient.createSnapshot();
        System.out.println("Complete creating the share snpashot with snapshot id: " + response.getSnapshot());
        // END: com.azure.storage.file.share.ShareClient.createSnapshot
    }

    /**
     * Generates a code sample for using {@link ShareClient#createSnapshotWithResponse(Map, Duration, Context)}
     */
    public void createSnapshotWithMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.createSnapshotWithResponse#map-duration-context
        Response<ShareSnapshotInfo> response =
            shareClient.createSnapshotWithResponse(Collections.singletonMap("snpashot", "metadata"),
                Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete creating the share snpashot with snapshot id: " + response.getValue().getSnapshot());
        // END: com.azure.storage.file.share.ShareClient.createSnapshotWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#createFileWithResponse(String, long, ShareFileHttpHeaders, FileSmbProperties, String, Map, Duration, Context)}
     */
    public void createFileWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-Duration-Context
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
        Response<ShareFileClient> response = shareClient.createFileWithResponse("myfile", 1024,
            httpHeaders, smbProperties, filePermission, Collections.singletonMap("directory", "metadata"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Creating the file completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteDirectory(String)}
     */
    public void deleteDirectory() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.deleteDirectory#string
        shareClient.deleteDirectory("mydirectory");
        System.out.println("Completed deleting the directory.");
        // END: com.azure.storage.file.share.ShareClient.deleteDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteDirectoryWithResponse(String, Duration, Context)}
     */
    public void deleteDirectoryWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.deleteDirectoryWithResponse#string-duration-context
        Response<Void> response = shareClient.deleteDirectoryWithResponse("mydirectory",
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the directory with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareClient.deleteDirectoryWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteFile(String)}
     */
    public void deleteFile() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.deleteFile#string
        shareClient.deleteFile("myfile");
        System.out.println("Complete deleting the file.");
        // END: com.azure.storage.file.share.ShareClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteFileWithResponse(String, Duration, Context)}
     */
    public void deleteFileWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.deleteFileWithResponse#string-duration-context
        Response<Void> response = shareClient.deleteFileWithResponse("myfile",
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareClient.deleteFileWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#delete}
     */
    public void deleteShare() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.delete
        shareClient.delete();
        System.out.println("Completed deleting the share.");
        // END: com.azure.storage.file.share.ShareClient.delete
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteWithResponse(Duration, Context)}
     */
    public void deleteShareWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.deleteWithResponse#duration-context
        Response<Void> response = shareClient.deleteWithResponse(Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the share with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareClient.deleteWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#getProperties()}
     */
    public void getProperties() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.getProperties
        ShareProperties properties = shareClient.getProperties();
        System.out.printf("Share quota: %d, Metadata: %s", properties.getQuota(), properties.getMetadata());
        // END: com.azure.storage.file.share.ShareClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.getPropertiesWithResponse#duration-context
        ShareProperties properties = shareClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1)).getValue();
        System.out.printf("Share quota: %d, Metadata: %s", properties.getQuota(), properties.getMetadata());
        // END: com.azure.storage.file.share.ShareClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#setQuota(int)}
     */
    public void setQuota() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: ShareClient.setQuota#int
        System.out.println("Setting the share quota completed." + shareClient.setQuota(1024));
        // END: ShareClient.setQuota#int
    }

    /**
     * Generates a code sample for using {@link ShareClient#setQuotaWithResponse(int, Duration, Context)}
     */
    public void setQuotaWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.setQuotaWithResponse#int-duration-context
        Response<ShareInfo> response = shareClient.setQuotaWithResponse(1024,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the share quota completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareClient.setQuotaWithResponse#int-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#setMetadata(Map)}
     */
    public void setMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.setMetadata#map
        shareClient.setMetadata(Collections.singletonMap("share", "updatedMetadata"));
        System.out.println("Setting the share metadata.");
        // END: com.azure.storage.file.share.ShareClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareClient#setMetadataWithResponse(Map, Duration, Context)}
     */
    public void setMetadataWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.setMetadataWithResponse#map-duration-context
        Response<ShareInfo> response = shareClient.setMetadataWithResponse(
            Collections.singletonMap("share", "updatedMetadata"), Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Setting the share metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareClient.setMetadataWithResponse#map-duration-context
    }


    /**
     * Generates a code sample for using {@link ShareClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.clearMetadata#map
        shareClient.setMetadata(null);
        System.out.println("Clear metadata completed.");
        // END: com.azure.storage.file.share.ShareClient.clearMetadata#map
    }


    /**
     * Generates a code sample for using {@link ShareClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.getAccessPolicy
        for (ShareSignedIdentifier result : shareClient.getAccessPolicy()) {
            System.out.printf("Access policy %s allows these permissions: %s",
                result.getId(), result.getAccessPolicy().getPermissions());
        }
        // END: com.azure.storage.file.share.ShareClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link ShareClient#setAccessPolicy(List)}
     */
    public void setAccessPolicy() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: ShareClient.setAccessPolicy#List
        ShareAccessPolicy accessPolicy = new ShareAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        ShareSignedIdentifier permission = new ShareSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);

        shareClient.setAccessPolicy(Collections.singletonList(permission));
        System.out.println("Setting access policies completed.");
        // END: ShareClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link ShareClient#setAccessPolicyWithResponse(List, Duration, Context)}
     */
    public void setAccessPolicyWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.setAccessPolicyWithResponse#list-duration-context
        ShareAccessPolicy accessPolicy = new ShareAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        ShareSignedIdentifier permission = new ShareSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);

        Response<ShareInfo> response = shareClient.setAccessPolicyWithResponse(Collections.singletonList(permission),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting access policies completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareClient.setAccessPolicyWithResponse#list-duration-context
    }


    /**
     * Generates a code sample for using {@link ShareClient#getStatistics()}
     */
    public void getStatistics() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.getStatistics
        ShareStatistics response = shareClient.getStatistics();
        System.out.printf("The share is using %d GB", response.getShareUsageInGB());
        // END: com.azure.storage.file.share.ShareClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link ShareClient#getStatisticsWithResponse(Duration, Context)}
     */
    public void getStatisticsWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.getStatisticsWithResponse#duration-context
        Response<ShareStatistics> response = shareClient.getStatisticsWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("The share is using %d GB", response.getValue().getShareUsageInGB());
        // END: com.azure.storage.file.share.ShareClient.getStatisticsWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#createPermission(String)}
     */
    public void createPermissionAsync() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.createPermission#string
        String response = shareClient.createPermission("filePermission");
        System.out.printf("The file permission key is %s", response);
        // END: com.azure.storage.file.share.ShareClient.createPermission#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#createPermissionWithResponse(String, Context)}
     */
    public void createPermissionWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.createPermissionWithResponse#string-context
        Response<String> response = shareClient.createPermissionWithResponse("filePermission", Context.NONE);
        System.out.printf("The file permission key is %s", response.getValue());
        // END: com.azure.storage.file.share.ShareClient.createPermissionWithResponse#string-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#getPermission(String)}
     */
    public void getPermission() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.getPermission#string
        String response = shareClient.getPermission("filePermissionKey");
        System.out.printf("The file permission is %s", response);
        // END: com.azure.storage.file.share.ShareClient.getPermission#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#getPermissionWithResponse(String, Context)}
     */
    public void getPermissionWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.getPermissionWithResponse#string-context
        Response<String> response = shareClient.getPermissionWithResponse("filePermissionKey", Context.NONE);
        System.out.printf("The file permission is %s", response.getValue());
        // END: com.azure.storage.file.share.ShareClient.getPermissionWithResponse#string-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#getSnapshotId()}
     */
    public void getSnapshotId() {
        // BEGIN: com.azure.storage.file.share.ShareClient.getSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareClient shareClient = new ShareClientBuilder().endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .snapshot(currentTime.toString())
            .buildClient();

        System.out.printf("Snapshot ID: %s%n", shareClient.getSnapshotId());
        // END: com.azure.storage.file.share.ShareClient.getSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareClient#getShareName()}
     */
    public void getShareName() {
        ShareClient shareAsyncClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareClient.getShareName
        String shareName = shareAsyncClient.getShareName();
        System.out.println("The name of the share is " + shareName);
        // END: com.azure.storage.file.share.ShareClient.getShareName
    }

    /**
     * Code snippet for {@link ShareClient#generateSas(ShareServiceSasSignatureValues)}
     */
    public void generateSas() {
        ShareClient shareClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareClient.generateSas#ShareServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        ShareSasPermission permission = new ShareSasPermission().setReadPermission(true);

        ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        shareClient.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.file.share.ShareClient.generateSas#ShareServiceSasSignatureValues
    }
}
