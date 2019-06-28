package com.azure.storage.files.javadoc;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.FileServiceClient;
import com.azure.storage.file.FileServiceAsyncClient;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.ShareClient;

/**
 * Contains code snippets when generating javadocs through doclets for {@link FileServiceClient} and {@link FileServiceAsyncClient}.
 */
public class FileServiceJavaDocCodeSamples {

    /**
     * Generates code sample for creating a {@link FileServiceClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileServiceClient}
     */
    public FileServiceClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.file.fileServiceClient.instantiation.sastoken
        FileServiceClient fileServiceClient = FileServiceClient.builder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .build();
        // END: com.azure.storage.file.fileServiceClient.instantiation.sastoken
        return fileServiceClient;
    }

    /**
     * Generates code sample for creating a {@link FileServiceAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileServiceAsyncClient}
     */
    public FileServiceAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.instantiation.sastoken
        FileServiceAsyncClient fileServiceAsyncClient = FileServiceAsyncClient.builder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .buildAsync();
        // END: com.azure.storage.file.fileServiceAsyncClient.instantiation.sastoken
        return fileServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileServiceClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileServiceClient}
     */
    public FileServiceClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileServiceClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};EndpointSuffix={core.windows.net}";
        FileServiceClient fileServiceClient = FileServiceClient.builder()
            .connectionString(connectionString)
            .build();
        // END: com.azure.storage.file.fileServiceClient.instantiation.connectionstring
        return fileServiceClient;
    }

    /**
     * Generates code sample for creating a {@link FileServiceAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileServiceAsyncClient}
     */
    public FileServiceAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};EndpointSuffix={core.windows.net}";
        FileServiceAsyncClient fileServiceAsyncClient = FileServiceAsyncClient.builder()
            .connectionString(connectionString)
            .buildAsync();
        // END: com.azure.storage.file.fileServiceAsyncClient.instantiation.connectionstring
        return fileServiceAsyncClient;
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#createShare(String)}
     */
    public void createShare() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.createShare#string
        fileServiceClient.createShare("myshare");
        // END: com.azure.storage.file.fileServiceClient.createShare#string
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#createShare(String)}
     */
    public void createShareAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.createShare#string
        fileServiceAsyncClient.createShare("myshare").subscribe(
            response -> {},
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.createShare#string
    }


    /**
     * Generates a code sample for using {@link FileServiceClient#listShares()}
     */
    public void listShares() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.listShares
        fileServiceClient.listShares().forEach(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.name())
        );
        // END: com.azure.storage.file.fileServiceClient.listShares
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#listShares()}
     */
    public void listSharesAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.listShares
        fileServiceAsyncClient.listShares().subscribe(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.name()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.listShares
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#deleteShare(String)}
     */
    public void deleteShare() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.deleteShare#string
        fileServiceClient.deleteShare("myshare");
        // END: com.azure.storage.file.fileServiceClient.deleteShare#string
    }


    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#deleteShare(String)}
     */
    public void deleteShareAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.deleteShare#string
        fileServiceAsyncClient.deleteShare("myshare").subscribe(
            response -> System.out.println("Deleting the share completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.file.fileServiceClient.deleteShare#string
    }
}
