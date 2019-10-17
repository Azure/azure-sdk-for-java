// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.models.FileAccessPolicy;
import com.azure.storage.file.models.FileHttpHeaders;
import com.azure.storage.file.models.FileSignedIdentifier;
import com.azure.storage.file.models.NtfsFileAttributes;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;

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
        // BEGIN: com.azure.storage.file.shareClient.instantiation
        ShareClient client = new ShareClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildClient();
        // END: com.azure.storage.file.shareClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link ShareClient} with SAS token
     * @return An instance of {@link ShareClient}
     */
    public ShareClient createClientWithSASToken() {

        // BEGIN: com.azure.storage.file.shareClient.instantiation.sastoken
        ShareClient shareClient = new ShareClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .buildClient();
        // END: com.azure.storage.file.shareClient.instantiation.sastoken
        return shareClient;
    }


    /**
     * Generates code sample for creating a {@link ShareClient} with SAS token
     * @return An instance of {@link ShareClient}
     */
    public ShareClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.shareClient.instantiation.credential
        ShareClient shareClient = new ShareClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .buildClient();
        // END: com.azure.storage.file.shareClient.instantiation.credential
        return shareClient;
    }

    /**
     * Generates code sample for creating a {@link ShareClient} with {@code connectionString} which turns into {@link StorageSharedKeyCredential}
     * @return An instance of {@link ShareClient}
     */
    public ShareClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.shareClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        ShareClient shareClient = new ShareClientBuilder()
            .connectionString(connectionString).shareName("myshare")
            .buildClient();
        // END: com.azure.storage.file.shareClient.instantiation.connectionstring
        return shareClient;
    }

    /**
     * Generates a code sample for using {@link ShareClient#create()}
     */
    public void createShare() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.create
        ShareInfo response = shareClient.create();
        System.out.println("Complete creating the shares with status code: " + response);
        // END: com.azure.storage.file.shareClient.create
    }

    /**
     * Generates a code sample for using {@link ShareClient#create()}.
     */
    public void createShareMaxOverloadMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.create#map-integer.metadata
        ShareInfo response = shareClient.create();
        System.out.println("Complete creating the shares.");
        // END: com.azure.storage.file.shareClient.create#map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link ShareClient#createWithResponse(Map, Integer,
     * Duration, Context)} with Quota.
     */
    public void createWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareClient.createWithResponse#map-integer-duration-context.quota
        Response<ShareInfo> response = shareClient.createWithResponse(null, 10,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete creating the shares with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.ShareClient.createWithResponse#map-integer-duration-context.quota
    }

    /**
     * Generates a code sample for using {@link ShareClient#createWithResponse(Map, Integer,
     * Duration, Context)} with Metadata.
     */
    public void createWithResponseMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareClient.createWithResponse#map-integer-duration-context.metadata
        Response<ShareInfo> response = shareClient.createWithResponse(Collections.singletonMap("share", "metadata"),
            null, Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete creating the shares with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.ShareClient.createWithResponse#map-integer-duration-context.metadata
    }

    /**
     * Generates a code sample for using {@link ShareClient#createDirectory(String)} ()}
     */
    public void createDirectory() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createDirectory#string
        DirectoryClient response = shareClient.createDirectory("mydirectory");
        System.out.println("Complete creating the directory.");
        // END: com.azure.storage.file.shareClient.createDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#createDirectoryWithResponse(String, FileSmbProperties, String, Map, Duration, Context)}
     */
    public void createDirectoryWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createDirectoryWithResponse#string-filesmbproperties-string-map-duration-context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<DirectoryClient> response = shareClient.createDirectoryWithResponse("documents",
            smbProperties, filePermission, Collections.singletonMap("directory", "metadata"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Creating the directory completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.shareClient.createDirectoryWithResponse#string-filesmbproperties-string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#createFile(String, long)}
     */
    public void createFile() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createFile#string-long
        FileClient response = shareClient.createFile("myfile", 1024);
        System.out.println("Complete creating the file with snapshot Id:" + response.getShareSnapshotId());
        // END: com.azure.storage.file.shareClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link ShareClient#createSnapshot()}
     */
    public void createSnapshot() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createSnapshot
        ShareSnapshotInfo response = shareClient.createSnapshot();
        System.out.println("Complete creating the share snpashot with snapshot id: " + response.getSnapshot());
        // END: com.azure.storage.file.shareClient.createSnapshot
    }

    /**
     * Generates a code sample for using {@link ShareClient#createSnapshotWithResponse(Map, Duration, Context)}
     */
    public void createSnapshotWithMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createSnapshotWithResponse#map-duration-context
        Response<ShareSnapshotInfo> response =
            shareClient.createSnapshotWithResponse(Collections.singletonMap("snpashot", "metadata"),
                Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete creating the share snpashot with snapshot id: " + response.getValue().getSnapshot());
        // END: com.azure.storage.file.shareClient.createSnapshotWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#createFileWithResponse(String, long, FileHttpHeaders, FileSmbProperties, String, Map, Duration, Context)}
     */
    public void createFileWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createFileWithResponse#string-long-filehttpheaders-filesmbproperties-string-map-duration-context
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
            .setFileContentType("text/html")
            .setFileContentEncoding("gzip")
            .setFileContentLanguage("en")
            .setFileCacheControl("no-transform")
            .setFileContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        Response<FileClient> response = shareClient.createFileWithResponse("myfile", 1024,
            httpHeaders, smbProperties, filePermission, Collections.singletonMap("directory", "metadata"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Creating the file completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.shareClient.createFileWithResponse#string-long-filehttpheaders-filesmbproperties-string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteDirectory(String)}
     */
    public void deleteDirectory() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteDirectory#string
        shareClient.deleteDirectory("mydirectory");
        System.out.println("Completed deleting the directory.");
        // END: com.azure.storage.file.shareClient.deleteDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteDirectoryWithResponse(String, Duration, Context)}
     */
    public void deleteDirectoryWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteDirectoryWithResponse#string-duration-context
        Response<Void> response = shareClient.deleteDirectoryWithResponse("mydirectory",
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the directory with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.shareClient.deleteDirectoryWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteFile(String)}
     */
    public void deleteFile() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteFile#string
        shareClient.deleteFile("myfile");
        System.out.println("Complete deleting the file.");
        // END: com.azure.storage.file.shareClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteFileWithResponse(String, Duration, Context)}
     */
    public void deleteFileWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteFileWithResponse#string-duration-context
        Response<Void> response = shareClient.deleteFileWithResponse("myfile",
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.shareClient.deleteFileWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#delete}
     */
    public void deleteShare() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.delete
        shareClient.delete();
        System.out.println("Completed deleting the share.");
        // END: com.azure.storage.file.shareClient.delete
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteWithResponse(Duration, Context)}
     */
    public void deleteShareWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteWithResponse#duration-context
        Response<Void> response = shareClient.deleteWithResponse(Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the share with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.shareClient.deleteWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#getProperties()}
     */
    public void getProperties() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getProperties
        ShareProperties properties = shareClient.getProperties();
        System.out.printf("Share quota: %d, Metadata: %s", properties.getQuota(), properties.getMetadata());
        // END: com.azure.storage.file.shareClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getPropertiesWithResponse#duration-context
        ShareProperties properties = shareClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1)).getValue();
        System.out.printf("Share quota: %d, Metadata: %s", properties.getQuota(), properties.getMetadata());
        // END: com.azure.storage.file.shareClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#setQuota(int)}
     */
    public void setQuota() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareClient.setQuota#int
        System.out.println("Setting the share quota completed." + shareClient.setQuota(1024));
        // END: com.azure.storage.file.ShareClient.setQuota#int
    }

    /**
     * Generates a code sample for using {@link ShareClient#setQuotaWithResponse(int, Duration, Context)}
     */
    public void setQuotaWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setQuotaWithResponse#int-duration-context
        Response<ShareInfo> response = shareClient.setQuotaWithResponse(1024,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the share quota completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.shareClient.setQuotaWithResponse#int-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#setMetadata(Map)}
     */
    public void setMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setMetadata#map
        shareClient.setMetadata(Collections.singletonMap("share", "updatedMetadata"));
        System.out.println("Setting the share metadata.");
        // END: com.azure.storage.file.shareClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareClient#setMetadataWithResponse(Map, Duration, Context)}
     */
    public void setMetadataWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setMetadataWithResponse#map-duration-context
        Response<ShareInfo> response = shareClient.setMetadataWithResponse(
            Collections.singletonMap("share", "updatedMetadata"), Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Setting the share metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.shareClient.setMetadataWithResponse#map-duration-context
    }


    /**
     * Generates a code sample for using {@link ShareClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.clearMetadata#map
        shareClient.setMetadata(null);
        System.out.println("Clear metadata completed.");
        // END: com.azure.storage.file.shareClient.clearMetadata#map
    }


    /**
     * Generates a code sample for using {@link ShareClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getAccessPolicy
        for (FileSignedIdentifier result : shareClient.getAccessPolicy()) {
            System.out.printf("Access policy %s allows these permissions: %s",
                result.getId(), result.getAccessPolicy().getPermissions());
        }
        // END: com.azure.storage.file.shareClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link ShareClient#setAccessPolicy(List)}
     */
    public void setAccessPolicy() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareClient.setAccessPolicy#List
        FileAccessPolicy accessPolicy = new FileAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        FileSignedIdentifier permission = new FileSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);

        shareClient.setAccessPolicy(Collections.singletonList(permission));
        System.out.println("Setting access policies completed.");
        // END: com.azure.storage.file.ShareClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link ShareClient#setAccessPolicyWithResponse(List, Duration, Context)}
     */
    public void setAccessPolicyWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setAccessPolicyWithResponse#list-duration-context
        FileAccessPolicy accessPolicy = new FileAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        FileSignedIdentifier permission = new FileSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);

        Response<ShareInfo> response = shareClient.setAccessPolicyWithResponse(Collections.singletonList(permission),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting access policies completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.shareClient.setAccessPolicyWithResponse#list-duration-context
    }


    /**
     * Generates a code sample for using {@link ShareClient#getStatistics()}
     */
    public void getStatistics() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getStatistics
        ShareStatistics response = shareClient.getStatistics();
        System.out.printf("The share is using %d GB", response.getShareUsageInGB());
        // END: com.azure.storage.file.shareClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link ShareClient#getStatisticsWithResponse(Duration, Context)}
     */
    public void getStatisticsWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getStatisticsWithResponse#duration-context
        Response<ShareStatistics> response = shareClient.getStatisticsWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("The share is using %d GB", response.getValue().getShareUsageInGB());
        // END: com.azure.storage.file.shareClient.getStatisticsWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#createPermission(String)}
     */
    public void createPermissionAsync() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createPermission#string
        String response = shareClient.createPermission("filePermission");
        System.out.printf("The file permission key is %s", response);
        // END: com.azure.storage.file.shareClient.createPermission#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#createPermissionWithResponse(String, Context)}
     */
    public void createPermissionWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createPermissionWithResponse#string-context
        Response<String> response = shareClient.createPermissionWithResponse("filePermission", Context.NONE);
        System.out.printf("The file permission key is %s", response.getValue());
        // END: com.azure.storage.file.shareClient.createPermissionWithResponse#string-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#getPermission(String)}
     */
    public void getPermission() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getPermission#string
        String response = shareClient.getPermission("filePermissionKey");
        System.out.printf("The file permission is %s", response);
        // END: com.azure.storage.file.shareClient.getPermission#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#getPermissionWithResponse(String, Context)}
     */
    public void getPermissionWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getPermissionWithResponse#string-context
        Response<String> response = shareClient.getPermissionWithResponse("filePermissionKey", Context.NONE);
        System.out.printf("The file permission is %s", response.getValue());
        // END: com.azure.storage.file.shareClient.getPermissionWithResponse#string-context
    }

    /**
     * Generates a code sample for using {@link ShareClient#getSnapshotId()}
     */
    public void getSnapshotId() {
        // BEGIN: com.azure.storage.file.shareClient.getSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareClient shareClient = new ShareClientBuilder().endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .snapshot(currentTime.toString())
            .buildClient();

        System.out.printf("Snapshot ID: %s%n", shareClient.getSnapshotId());
        // END: com.azure.storage.file.shareClient.getSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareClient#getShareName()}
     */
    public void getShareName() {
        ShareClient shareAsyncClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getShareName
        String shareName = shareAsyncClient.getShareName();
        System.out.println("The name of the share is " + shareName);
        // END: com.azure.storage.file.shareClient.getShareName
    }
}
