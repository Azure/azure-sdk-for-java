// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.core.util.Context;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.AccessPolicy;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareProperties;
import com.azure.storage.file.models.ShareSnapshotInfo;
import com.azure.storage.file.models.ShareStatistics;
import com.azure.storage.file.models.SignedIdentifier;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
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
     * Generates code sample for creating a {@link ShareClient} with {@link SASTokenCredential}
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
     * Generates code sample for creating a {@link ShareClient} with {@link SASTokenCredential}
     * @return An instance of {@link ShareClient}
     */
    public ShareClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.shareClient.instantiation.credential
        ShareClient shareClient = new ShareClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("${SASTokenQueryParams}")))
            .shareName("myshare")
            .buildClient();
        // END: com.azure.storage.file.shareClient.instantiation.credential
        return shareClient;
    }

    /**
     * Generates code sample for creating a {@link ShareClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
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
     * Generates a code sample for using {@link ShareClient#createWithResponse(Map, Integer, Context)} with Quota.
     */
    public void createWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareClient.createWithResponse#Map-Integer-Context.quota
        Response<ShareInfo> response = shareClient.createWithResponse(null, 10, new Context(key1, value1));
        System.out.println("Complete creating the shares with status code: " + response.statusCode());
        // END: com.azure.storage.file.ShareClient.createWithResponse#Map-Integer-Context.quota
    }

    /**
     * Generates a code sample for using {@link ShareClient#createWithResponse(Map, Integer, Context)} with Metadata.
     */
    public void createWithResponseMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareClient.createWithResponse#Map-Integer-Context.metadata
        Response<ShareInfo> response = shareClient.createWithResponse(Collections.singletonMap("share", "metadata"), null,
            new Context(key1, value1));
        System.out.println("Complete creating the shares with status code: " + response.statusCode());
        // END: com.azure.storage.file.ShareClient.createWithResponse#Map-Integer-Context.metadata
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
     * Generates a code sample for using {@link ShareClient#createDirectoryWithResponse(String, Map, Context)}
     */
    public void createDirectoryWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createDirectoryWithResponse#string-map-Context
        Response<DirectoryClient> response = shareClient.createDirectoryWithResponse("documents",
            Collections.singletonMap("directory", "metadata"), new Context(key1, value1));
        System.out.printf("Creating the directory completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.createDirectoryWithResponse#string-map-Context
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
        System.out.println("Complete creating the share snpashot with snapshot id: " + response.snapshot());
        // END: com.azure.storage.file.shareClient.createSnapshot
    }

    /**
     * Generates a code sample for using {@link ShareClient#createSnapshotWithResponse(Map, Context)}
     */
    public void createSnapshotWithMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createSnapshotWithResponse#map-Context
        Response<ShareSnapshotInfo> response =
            shareClient.createSnapshotWithResponse(Collections.singletonMap("snpashot", "metadata"), new Context(key1, value1));
        System.out.println("Complete creating the share snpashot with snapshot id: " + response.value().snapshot());
        // END: com.azure.storage.file.shareClient.createSnapshotWithResponse#map-Context
    }

    /**
     * Generates a code sample for using {@link ShareClient#createFileWithResponse(String, long, FileHTTPHeaders, Map, Context)}
     */
    public void createFileWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createFileWithResponse#string-long-filehttpheaders-map-Context
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        Response<FileClient> response = shareClient.createFileWithResponse("myfile", 1024, httpHeaders,
            Collections.singletonMap("directory", "metadata"), new Context(key1, value1));
        System.out.printf("Creating the file completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.createFileWithResponse#string-long-filehttpheaders-map-Context
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
     * Generates a code sample for using {@link ShareClient#deleteDirectoryWithResponse(String, Context)}
     */
    public void deleteDirectoryWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteDirectoryWithResponse#string-Context
        VoidResponse response = shareClient.deleteDirectoryWithResponse("mydirectory", new Context(key1, value1));
        System.out.println("Complete deleting the directory with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.deleteDirectoryWithResponse#string-Context
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
     * Generates a code sample for using {@link ShareClient#deleteFileWithResponse(String, Context)}
     */
    public void deleteFileWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteFileWithResponse#string-Context
        VoidResponse response = shareClient.deleteFileWithResponse("myfile", new Context(key1, value1));
        System.out.println("Complete deleting the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.deleteFileWithResponse#string-Context
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
     * Generates a code sample for using {@link ShareClient#delete}
     */
    public void deleteShareWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteWithResponse#Context
        VoidResponse response = shareClient.deleteWithResponse(new Context(key1, value1));
        System.out.println("Complete deleting the share with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.deleteWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link ShareClient#getProperties()}
     */
    public void getProperties() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getProperties
        ShareProperties properties = shareClient.getProperties();
        System.out.printf("Share quota: %d, Metadata: %s", properties.quota(), properties.metadata());
        // END: com.azure.storage.file.shareClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareClient#getPropertiesWithResponse(Context)}
     */
    public void getPropertiesWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getPropertiesWithResponse#Context
        ShareProperties properties = shareClient.getPropertiesWithResponse(new Context(key1, value1)).value();
        System.out.printf("Share quota: %d, Metadata: %s", properties.quota(), properties.metadata());
        // END: com.azure.storage.file.shareClient.getPropertiesWithResponse#Context
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
     * Generates a code sample for using {@link ShareClient#setQuotaWithResponse(int, Context)}
     */
    public void setQuotaWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setQuotaWithResponse#int-Context
        Response<ShareInfo> response = shareClient.setQuotaWithResponse(1024, new Context(key1, value1));
        System.out.printf("Setting the share quota completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.setQuotaWithResponse#int-Context
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
     * Generates a code sample for using {@link ShareClient#setMetadataWithResponse(Map, Context)}
     */
    public void setMetadataWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setMetadataWithResponse#map-Context
        Response<ShareInfo> response = shareClient.setMetadataWithResponse(
            Collections.singletonMap("share", "updatedMetadata"),
            new Context(key1, value1));
        System.out.printf("Setting the share metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.setMetadataWithResponse#map-Context
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
        for (SignedIdentifier result : shareClient.getAccessPolicy()) {
            System.out.printf("Access policy %s allows these permissions: %s", result.id(), result.accessPolicy().permission());
        }
        // END: com.azure.storage.file.shareClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link ShareClient#setAccessPolicy(List)}
     */
    public void setAccessPolicy() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareClient.setAccessPolicy#List
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);

        shareClient.setAccessPolicy(Collections.singletonList(permission));
        System.out.println("Setting access policies completed.");
        // END: com.azure.storage.file.ShareClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link ShareClient#setAccessPolicyWithResponse(List, Context)}
     */
    public void setAccessPolicyWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setAccessPolicyWithResponse#List-Context
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);

        Response<ShareInfo> response = shareClient.setAccessPolicyWithResponse(Collections.singletonList(permission),
            new Context(key1, value1));
        System.out.printf("Setting access policies completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.setAccessPolicyWithResponse#List-Context
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
     * Generates a code sample for using {@link ShareClient#getStatisticsWithResponse(Context)}
     */
    public void getStatisticsWithResponse() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getStatisticsWithResponse#Context
        Response<ShareStatistics> response = shareClient.getStatisticsWithResponse(new Context(key1, value1));
        System.out.printf("The share is using %d GB", response.value().getShareUsageInGB());
        // END: com.azure.storage.file.shareClient.getStatisticsWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link ShareClient#getSnapshotId()}
     */
    public void getSnapshotId() {
        // BEGIN: com.azure.storage.file.shareClient.getSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareClient shareClient = new ShareClientBuilder().endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromSASTokenString("${SASToken}"))
            .shareName("myshare")
            .snapshot(currentTime.toString())
            .buildClient();

        System.out.printf("Snapshot ID: %s%n", shareClient.getSnapshotId());
        // END: com.azure.storage.file.shareClient.getSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareClient#generateSAS(String, ShareSASPermission, OffsetDateTime,
     * OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateSAS() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.ShareClient.generateSAS
        String identifier = "identifier";
        ShareSASPermission permissions = new ShareSASPermission()
            .read(true)
            .create(true)
            .delete(true)
            .write(true)
            .list(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;
        String sas = shareClient.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol,
            ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
        // END: com.azure.storage.file.ShareClient.generateSAS
    }
}
