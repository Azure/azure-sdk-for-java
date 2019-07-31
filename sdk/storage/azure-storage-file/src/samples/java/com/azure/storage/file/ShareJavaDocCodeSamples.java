// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
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
 * Contains code snippets when generating javadocs through doclets for {@link ShareClient} and {@link ShareAsyncClient}.
 */
public class ShareJavaDocCodeSamples {
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
     * Generates code sample for creating a {@link ShareAsyncClient} with {@link SASTokenCredential}
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
     * Generates code sample for creating a {@link ShareClient} with {@link SASTokenCredential}
     * @return An instance of {@link ShareClient}
     */
    public ShareClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.shareClient.instantiation.credential
        ShareClient shareClient = new ShareClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .shareName("myshare")
            .buildClient();
        // END: com.azure.storage.file.shareClient.instantiation.credential
        return shareClient;
    }

    /**
     * Generates code sample for creating a {@link ShareAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link ShareAsyncClient}
     */
    public ShareAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.shareAsyncClient.instantiation.credential
        ShareAsyncClient shareAsyncClient = new ShareClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .shareName("myshare")
            .buildAsyncClient();
        // END: com.azure.storage.file.shareAsyncClient.instantiation.credential
        return shareAsyncClient;
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
     * Generates code sample for creating a {@link ShareAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
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
     * Generates a code sample for using {@link ShareClient#create()}
     */
    public void createShare() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.create
        Response<ShareInfo> response = shareClient.create();
        System.out.println("Complete creating the shares with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.create
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#create}
     */
    public void createShareAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.create
        shareAsyncClient.create().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.shareAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link ShareClient#create(Map, Integer)} with Metadata.
     */
    public void createShareMaxOverloadMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.create#map-integer.metadata
        Response<ShareInfo> response = shareClient.create(Collections.singletonMap("share", "metadata"), null);
        System.out.println("Complete creating the shares with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.create#map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#create(Map, Integer)} with Metadata.
     */
    public void createShareAsyncMaxOverloadMetadata() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.create#map-integer.metadata
        shareAsyncClient.create(Collections.singletonMap("share", "metadata"), null).subscribe(
            response -> System.out.printf("Creating the share completed with status code %d", response.statusCode()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.shareAsyncClient.create#map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link ShareClient#create(Map, Integer)} with Quota.
     */
    public void createShareMaxOverloadQuota() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.create#map-integer.quota
        Response<ShareInfo> response = shareClient.create(null, 10);
        System.out.println("Complete creating the shares with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.create#map-integer.quota
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#create(Map, Integer)} with Quota.
     */
    public void createShareAsyncMaxOverloadQuota() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.create#map-integer.quota
        shareAsyncClient.create(null, 10).subscribe(
            response -> System.out.printf("Creating the share completed with status code %d", response.statusCode()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.shareAsyncClient.create#map-integer.quota
    }

    /**
     * Generates a code sample for using {@link ShareClient#createDirectory(String)} ()}
     */
    public void createDirectory() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createDirectory#string
        Response<DirectoryClient> response = shareClient.createDirectory("mydirectory");
        System.out.println("Complete creating the directory with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.createDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createDirectory(String)}
     */
    public void createDirectoryAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createDirectory#string
        shareAsyncClient.createDirectory("mydirectory").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the directory!")
        );
        // END: com.azure.storage.file.shareAsyncClient.createDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#createFile(String, long)}
     */
    public void createFile() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createFile#string-long
        Response<FileClient> response = shareClient.createFile("myfile", 1024);
        System.out.println("Complete creating the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createFile(String, long)}
     */
    public void createFileAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createFile#string-long
        shareAsyncClient.createFile("myfile", 1024).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the directory!")
        );
        // END: com.azure.storage.file.shareAsyncClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link ShareClient#createSnapshot()()}
     */
    public void createSnapshot() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createSnapshot
        Response<ShareSnapshotInfo> response = shareClient.createSnapshot();
        System.out.println("Complete creating the share snpashot with snapshot id: " + response.value().snapshot());
        // END: com.azure.storage.file.shareClient.createSnapshot
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createSnapshot()}
     */
    public void createSnapshotAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createSnapshot
        shareAsyncClient.createSnapshot().subscribe(
            response -> System.out.println("Successfully creating the share snapshot with snapshot id: "
                + response.value().snapshot()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete creating the share snapshot.")
        );
        // END: com.azure.storage.file.shareAsyncClient.createSnapshot
    }

    /**
     * Generates a code sample for using {@link ShareClient#createSnapshot(Map)}
     */
    public void createSnapshotWithMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createSnapshot#map
        Response<ShareSnapshotInfo> response =
            shareClient.createSnapshot(Collections.singletonMap("snpashot", "metadata"));
        System.out.println("Complete creating the share snpashot with snapshot id: " + response.value().snapshot());
        // END: com.azure.storage.file.shareClient.createSnapshot#map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createSnapshot(Map)}
     */
    public void createSnapshotAsyncWithMetadata() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createSnapshot#map
        shareAsyncClient.createSnapshot(Collections.singletonMap("snapshot", "metadata")).subscribe(
            response -> System.out.println("Successfully creating the share snapshot with snapshot id: "
                + response.value().snapshot()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete creating the share snapshot.")
        );
        // END: com.azure.storage.file.shareAsyncClient.createSnapshot#map
    }

    /**
     * Generates a code sample for using {@link ShareClient#createDirectory(String, Map)}
     */
    public void createDirectoryMaxOverload() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createDirectory#string-map
        Response<DirectoryClient> response = shareClient.createDirectory("documents",
            Collections.singletonMap("directory", "metadata"));
        System.out.printf("Creating the directory completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.createDirectory#string-map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createDirectory(String, Map)}
     */
    public void createDirectoryAsyncMaxOverload() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createDirectory#string-map
        shareAsyncClient.createDirectory("documents", Collections.singletonMap("directory", "metadata"))
            .subscribe(response -> System.out.printf("Creating the directory completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.shareAsyncClient.createDirectory#string-map
    }

    /**
     * Generates a code sample for using {@link ShareClient#createFile(String, long, FileHTTPHeaders, Map)}
     */
    public void createFileMaxOverload() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.createFile#string-long-filehttpheaders-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        Response<FileClient> response = shareClient.createFile("myfile", 1024, httpHeaders,
            Collections.singletonMap("directory", "metadata"));
        System.out.printf("Creating the file completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.createFile#string-long-filehttpheaders-map
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#createFile(String, long, FileHTTPHeaders, Map)}
     */
    public void createFileAsyncMaxOverload() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.createFile#string-long-filehttpheaders-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        shareAsyncClient.createFile("myfile", 1024, httpHeaders,
            Collections.singletonMap("directory", "metadata"))
            .subscribe(response -> System.out.printf("Creating the file completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.shareAsyncClient.createFile#string-long-filehttpheaders-map
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteDirectory(String)()}
     */
    public void deleteDirectory() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteDirectory#string
        VoidResponse response = shareClient.deleteDirectory("mydirectory");
        System.out.println("Complete deleting the directory with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.deleteDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#deleteDirectory(String)()}
     */
    public void deleteDirectoryAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.deleteDirectory#string
        shareAsyncClient.deleteDirectory("mydirectory").subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the directory.")
        );
        // END: com.azure.storage.file.shareAsyncClient.deleteDirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#deleteFile(String)()}
     */
    public void deleteFile() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.deleteFile#string
        VoidResponse response = shareClient.deleteFile("myfile");
        System.out.println("Complete deleting the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#deleteFile(String)()}
     */
    public void deleteFileAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.deleteFile#string
        shareAsyncClient.deleteFile("myfile").subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the file.")
        );
        // END: com.azure.storage.file.shareAsyncClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link ShareClient#delete}
     */
    public void deleteShare() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.delete
        VoidResponse response = shareClient.delete();
        System.out.println("Complete deleting the share with status code: " + response.statusCode());
        // END: com.azure.storage.file.shareClient.delete
    }


    /**
     * Generates a code sample for using {@link ShareAsyncClient#delete}
     */
    public void deleteShareAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.delete
        shareAsyncClient.delete().subscribe(
            response -> System.out.println("Deleting the shareAsyncClient completed with status code: "
                + response.statusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the share.")
        );
        // END: com.azure.storage.file.shareAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link ShareClient#getProperties()}
     */
    public void getProperties() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getProperties
        ShareProperties properties = shareClient.getProperties().value();
        System.out.printf("Share quota: %d, Metadata: %s", properties.quota(), properties.metadata());
        // END: com.azure.storage.file.shareClient.getProperties
    }


    /**
     * Generates a code sample for using {@link ShareAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getProperties
        shareAsyncClient.getProperties()
            .subscribe(response -> {
                ShareProperties properties = response.value();
                System.out.printf("Share quota: %d, Metadata: %s", properties.quota(), properties.metadata());
            });
        // END: com.azure.storage.file.shareAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareClient#setQuota(int)}
     */
    public void setQuota() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setQuota
        Response<ShareInfo> response = shareClient.setQuota(1024);
        System.out.printf("Setting the share quota completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.setQuota
    }


    /**
     * Generates a code sample for using {@link ShareAsyncClient#setQuota(int)}
     */
    public void setQuotaAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.setQuota
        shareAsyncClient.setQuota(1024)
            .subscribe(response ->
                System.out.printf("Setting the share quota completed with status code %d", response.statusCode())
            );
        // END: com.azure.storage.file.shareAsyncClient.setQuota
    }

    /**
     * Generates a code sample for using {@link ShareClient#setMetadata(Map)}
     */
    public void setMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setMetadata#map
        Response<ShareInfo> response = shareClient.setMetadata(Collections.singletonMap("share", "updatedMetadata"));
        System.out.printf("Setting the share metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.setMetadata#map
    }


    /**
     * Generates a code sample for using {@link ShareAsyncClient#setMetadata(Map)}
     */
    public void setMetadataAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.setMetadata#map
        shareAsyncClient.setMetadata(Collections.singletonMap("share", "updatedMetadata"))
            .subscribe(response ->
                System.out.printf("Setting the share metadata completed with status code %d", response.statusCode())
            );
        // END: com.azure.storage.file.shareAsyncClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadata() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.clearMetadata#map
        Response<ShareInfo> response = shareClient.setMetadata(null);
        System.out.printf("Setting the share metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.clearMetadata#map
    }


    /**
     * Generates a code sample for using {@link ShareAsyncClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadataAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.clearMetadata#map
        shareAsyncClient.setMetadata(null)
            .subscribe(response ->
                System.out.printf("Setting the share metadata completed with status code %d", response.statusCode())
            );
        // END: com.azure.storage.file.shareAsyncClient.clearMetadata#map
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
     * Generates a code sample for using {@link ShareAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicyAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getAccessPolicy
        shareAsyncClient.getAccessPolicy()
            .subscribe(result -> System.out.printf("Access policy %s allows these permissions: %s", result.id(),
                result.accessPolicy().permission())
            );
        // END: com.azure.storage.file.shareAsyncClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link ShareClient#setAccessPolicy(List)}
     */
    public void setAccessPolicy() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.setAccessPolicy
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);

        Response<ShareInfo> response = shareClient.setAccessPolicy(Collections.singletonList(permission));
        System.out.printf("Setting access policies completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.shareClient.setAccessPolicy
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#setAccessPolicy(List)}
     */
    public void setAccessPolicyAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.setAccessPolicy
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
        shareAsyncClient.setAccessPolicy(Collections.singletonList(permission))
            .subscribe(response -> System.out.printf("Setting access policies completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.shareAsyncClient.setAccessPolicy
    }

    /**
     * Generates a code sample for using {@link ShareClient#getStatistics()}
     */
    public void getStatistics() {
        ShareClient shareClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareClient.getStatistics
        Response<ShareStatistics> response = shareClient.getStatistics();
        System.out.printf("The share is using %d GB", response.value().getShareUsageInGB());
        // END: com.azure.storage.file.shareClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getStatistics()}
     */
    public void getStatisticsAsync() {
        ShareAsyncClient shareAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.shareAsyncClient.getStatistics
        shareAsyncClient.getStatistics()
            .subscribe(response -> System.out.printf("The share is using %d GB",
                response.value().getShareUsageInGB()));
        // END: com.azure.storage.file.shareAsyncClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link ShareClient#getSnapshotId()}
     */
    public void getSnapshotId() {
        // BEGIN: com.azure.storage.file.shareClient.getSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareClient shareClient = new ShareClientBuilder().endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASToken}"))
            .shareName("myshare")
            .snapshot(currentTime.toString())
            .buildClient();
        shareClient.getSnapshotId();
        // END: com.azure.storage.file.shareClient.getSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareAsyncClient#getSnapshotId()}
     */
    public void getSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.shareAsyncClient.getSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareAsyncClient shareAysncClient = new ShareClientBuilder().endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASToken}"))
            .shareName("myshare")
            .snapshot(currentTime.toString())
            .buildAsyncClient();
        shareAysncClient.getSnapshotId();
        // END: com.azure.storage.file.shareAsyncClient.getSnapshotId
    }
}
