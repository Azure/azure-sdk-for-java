// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.ShareInfo;
import com.azure.storage.file.models.ShareSnapshotInfo;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ShareClient} and {@link ShareAsyncClient}.
 */
public class ShareJavaDocCodeSamples {

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
            () -> System.out.println("Complete creating the directory.")
        );
        // END: com.azure.storage.file.shareAsyncClient.deleteDirectory#string
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
            () -> System.out.println("Complete creating the share.")
        );
        // END: com.azure.storage.file.shareAsyncClient.delete
    }
}
