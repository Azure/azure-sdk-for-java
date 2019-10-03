// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.AccessPolicy;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.NtfsFileAttributes;
import com.azure.storage.file.models.SignedIdentifier;

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
        // BEGIN: com.azure.storage.file.shareAsyncClient.instantiation
        ShareAsyncClient client = new ShareClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildAsyncClient();
        // END: com.azure.storage.file.shareAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link ShareAsyncClient} with SAS token.
     *
     * @return An instance of {@link ShareAsyncClient}
     */
    public ShareAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.shareAsyncClient.instantiation.sastoken
        ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .shareName("myshare")
            .buildAsyncClient();
        // END: com.azure.storage.file.shareAsyncClient.instantiation.sastoken
        return shareAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareAsyncClient} with SAS token.
     *
     * @return An instance of {@link ShareAsyncClient}
     */
    public ShareAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.shareAsyncClient.instantiation.credential
        ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .buildAsyncClient();
        // END: com.azure.storage.file.shareAsyncClient.instantiation.credential
        return shareAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareAsyncClient} with {@code connectionString} which turns into
     * {@link SharedKeyCredential}
     *
     * @return An instance of {@link ShareAsyncClient}
     */
    public ShareAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.shareAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
            .connectionString(connectionString).shareName("myshare")
            .buildAsyncClient();
        // END: com.azure.storage.file.shareAsyncClient.instantiation.connectionstring
        return shareAsyncClient;
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#create}
     */
    public void createShareAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.create
        shareAsyncClient.create().subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.shareAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createWithResponse(Map, Integer)} with Metadata.
     */
    public void createWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createWithResponse#map-integer.metadata
        shareAsyncClient.createWithResponse(Collections.singletonMap("share", "metadata"), null).subscribe(
            response -> System.out.printf("Creating the share completed with status code %d", response.getStatusCode()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.shareAsyncClient.createWithResponse#map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createWithResponse(Map, Integer)} with Quota.
     */
    public void createShareAsyncMaxOverloadQuota() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createWithResponse#map-integer.quota
        shareAsyncClient.createWithResponse(null, 10).subscribe(
            response -> System.out.printf("Creating the share completed with status code %d", response.getStatusCode()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.shareAsyncClient.createWithResponse#map-integer.quota
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createDirectory(String)}
     */
    public void createDirectoryAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createDirectory#string
        shareAsyncClient.createDirectory("mydirectory").subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the directory!")
        );
        // END: com.azure.storage.file.shareAsyncClient.createDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createFile(String, long)}
     */
    public void createFileAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createFile#string-long
        shareAsyncClient.createFile("myfile", 1024).subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the directory!")
        );
        // END: com.azure.storage.file.shareAsyncClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createSnapshot()}
     */
    public void createSnapshotAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createSnapshot
        shareAsyncClient.createSnapshot().subscribe(
            response -> System.out.println("Successfully creating the share snapshot with snapshot id: "
                + response.getSnapshot()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete creating the share snapshot.")
        );
        // END: com.azure.storage.file.shareAsyncClient.createSnapshot
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createSnapshotWithResponse(Map)}
     */
    public void createSnapshotAsyncWithMetadata() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createSnapshotWithResponse#map
        shareAsyncClient.createSnapshotWithResponse(Collections.singletonMap("snapshot", "metadata")).subscribe(
            response -> System.out.println("Successfully creating the share snapshot with snapshot id: "
                + response.getValue().getSnapshot()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete creating the share snapshot.")
        );
        // END: com.azure.storage.file.shareAsyncClient.createSnapshotWithResponse#map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createDirectoryWithResponse(String, FileSmbProperties,
     * String, Map)}
     */
    public void createDirectoryWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createDirectoryWithResponse#string-filesmbproperties-string-map
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        shareAsyncClient.createDirectoryWithResponse("documents", smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"))
            .subscribe(response -> System.out.printf("Creating the directory completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.shareAsyncClient.createDirectoryWithResponse#string-filesmbproperties-string-map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createFile(String, long)}
     */
    public void createFileAsyncMaxOverload() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createFile#string-long-filehttpheaders-map
        shareAsyncClient.createFile("myfile", 1024)
            .doOnSuccess(response -> System.out.println("Creating the file completed."));
        // END: com.azure.storage.file.shareAsyncClient.createFile#string-long-filehttpheaders-map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createFileWithResponse(String, long, FileHTTPHeaders,
     * FileSmbProperties, String, Map)}
     */
    public void createFileWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createFileWithResponse#string-long-filehttpheaders-filesmbproperties-string-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders()
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
        shareAsyncClient.createFileWithResponse("myfile", 1024, httpHeaders, smbProperties,
            filePermission, Collections.singletonMap("directory", "metadata"))
            .subscribe(response -> System.out.printf("Creating the file completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.shareAsyncClient.createFileWithResponse#string-long-filehttpheaders-filesmbproperties-string-map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#deleteDirectory(String)}
     */
    public void deleteDirectoryAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.deleteDirectory#string
        shareAsyncClient.deleteDirectory("mydirectory").subscribe(
            response -> {
            },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the directory.")
        );
        // END: com.azure.storage.file.shareAsyncClient.deleteDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#deleteFile(String)}
     */
    public void deleteFileAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.deleteFile#string
        shareAsyncClient.deleteFile("myfile").subscribe(
            response -> {
            },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the file.")
        );
        // END: com.azure.storage.file.shareAsyncClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#delete}
     */
    public void deleteShareAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.delete
        shareAsyncClient.delete().subscribe(
            response -> System.out.println("Deleting the shareAsyncClient completed."),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the share.")
        );
        // END: com.azure.storage.file.shareAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#deleteWithResponse()}
     */
    public void deleteWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.deleteWithResponse
        shareAsyncClient.deleteWithResponse().subscribe(
            response -> System.out.println("Deleting the shareAsyncClient completed with status code: "
                + response.getStatusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the share.")
        );
        // END: com.azure.storage.file.shareAsyncClient.deleteWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getProperties
        shareAsyncClient.getProperties()
            .subscribe(properties -> {
                System.out.printf("Share quota: %d, Metadata: %s", properties.getQuota(), properties.getMetadata());
            });
        // END: com.azure.storage.file.shareAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getPropertiesWithResponse
        shareAsyncClient.getPropertiesWithResponse()
            .subscribe(properties -> {
                System.out.printf("Share quota: %d, Metadata: %s", properties.getValue().getQuota(),
                    properties.getValue().getMetadata());
            });
        // END: com.azure.storage.file.shareAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setQuota(int)}
     */
    public void setQuotaAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareAsyncClient.setQuota#int
        shareAsyncClient.setQuota(1024).doOnSuccess(response ->
            System.out.println("Setting the share quota completed.")
        );
        // END: com.azure.storage.file.ShareAsyncClient.setQuota#int
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setQuotaWithResponse(int)}
     */
    public void setQuotaWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareAsyncClient.setQuotaWithResponse#int
        shareAsyncClient.setQuotaWithResponse(1024)
            .subscribe(response ->
                System.out.printf("Setting the share quota completed with status code %d", response.getStatusCode())
            );
        // END: com.azure.storage.file.ShareAsyncClient.setQuotaWithResponse#int
    }


    /**
     * Generates a code sample for using {@link ShareAsyncClient#setMetadata(Map)}
     */
    public void setMetadataAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.setMetadata#map
        shareAsyncClient.setMetadata(Collections.singletonMap("share", "updatedMetadata")).doOnSuccess(response ->
            System.out.println("Setting the share metadata completed.")
        );
        // END: com.azure.storage.file.shareAsyncClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setMetadataWithResponse(Map)}
     */
    public void setMetadataWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.setMetadataWithResponse#map
        shareAsyncClient.setMetadataWithResponse(Collections.singletonMap("share", "updatedMetadata"))
            .subscribe(response ->
                System.out.printf("Setting the share metadata completed with status code %d", response.getStatusCode())
            );
        // END: com.azure.storage.file.shareAsyncClient.setMetadataWithResponse#map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadataAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.clearMetadata#map
        shareAsyncClient.setMetadata(null).doOnSuccess(response ->
            System.out.println("Setting the share metadata completed.")
        );
        // END: com.azure.storage.file.shareAsyncClient.clearMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicyAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getAccessPolicy
        shareAsyncClient.getAccessPolicy()
            .subscribe(result -> System.out.printf("Access policy %s allows these permissions: %s", result.getId(),
                result.getAccessPolicy().getPermission())
            );
        // END: com.azure.storage.file.shareAsyncClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setAccessPolicy(List)}
     */
    public void setAccessPolicyAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareAsyncClient.setAccessPolicy#List
        AccessPolicy accessPolicy = new AccessPolicy().setPermission("r")
            .setStart(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        shareAsyncClient.setAccessPolicy(Collections.singletonList(permission)).doOnSuccess(
            response -> System.out.println("Setting access policies completed."));
        // END: com.azure.storage.file.ShareAsyncClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setAccessPolicyWithResponse(List)}
     */
    public void setAccessPolicyWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareAsyncClient.setAccessPolicyWithResponse#List
        AccessPolicy accessPolicy = new AccessPolicy().setPermission("r")
            .setStart(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        shareAsyncClient.setAccessPolicyWithResponse(Collections.singletonList(permission))
            .subscribe(response -> System.out.printf("Setting access policies completed completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.ShareAsyncClient.setAccessPolicyWithResponse#List
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getStatistics()}
     */
    public void getStatisticsAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getStatistics
        shareAsyncClient.getStatistics().doOnSuccess(response -> System.out.printf("The share is using %d GB",
            response.getShareUsageInGB()));
        // END: com.azure.storage.file.shareAsyncClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createPermission(String)}
     */
    public void createPermissionAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createPermission#string
        shareAsyncClient.createPermission("filePermission").subscribe(
            response -> System.out.printf("The file permission key is %s", response));
        // END: com.azure.storage.file.shareAsyncClient.createPermission#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createPermissionWithResponse(String)}
     */
    public void createPermissionWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createPermissionWithResponse#string
        shareAsyncClient.createPermissionWithResponse("filePermission").subscribe(
            response -> System.out.printf("The file permission key is %s", response.getValue()));
        // END: com.azure.storage.file.shareAsyncClient.createPermissionWithResponse#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getPermission(String)}
     */
    public void getPermissionAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getPermission#string
        shareAsyncClient.getPermission("filePermissionKey").subscribe(
            response -> System.out.printf("The file permission is %s", response));
        // END: com.azure.storage.file.shareAsyncClient.getPermission#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getPermissionWithResponse(String)}
     */
    public void getPermissionWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getPermissionWithResponse#string
        shareAsyncClient.getPermissionWithResponse("filePermissionKey").subscribe(
            response -> System.out.printf("The file permission is %s", response.getValue()));
        // END: com.azure.storage.file.shareAsyncClient.getPermissionWithResponse#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getStatisticsWithResponse()}
     */
    public void getStatisticsWithResponse() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getStatisticsWithResponse
        shareAsyncClient.getStatisticsWithResponse().subscribe(response -> System.out.printf("The share is using %d GB",
            response.getValue().getShareUsageInGB()));
        // END: com.azure.storage.file.shareAsyncClient.getStatisticsWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getSnapshotId()}
     */
    public void getSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.shareAsyncClient.getSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .snapshot(currentTime.toString())
            .buildAsyncClient();

        System.out.printf("Snapshot ID: %s%n", shareAsyncClient.getSnapshotId());
        // END: com.azure.storage.file.shareAsyncClient.getSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#generateSAS(String, ShareSASPermission, OffsetDateTime,
     * OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateSASAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.generateSAS#String-ShareSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
        String identifier = "identifier";
        ShareSASPermission permissions = new ShareSASPermission()
            .setRead(true)
            .setCreate(true)
            .setDelete(true)
            .setWrite(true)
            .setList(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;
        String sas = shareAsyncClient.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol,
            ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
        // END: com.azure.storage.file.shareAsyncClient.generateSAS#String-ShareSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getShareName()}
     */
    public void getShareNameAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getShareName
        String shareName = shareAsyncClient.getShareName();
        System.out.println("The name of the share is " + shareName);
        // END: com.azure.storage.file.shareAsyncClient.getShareName
    }
}
