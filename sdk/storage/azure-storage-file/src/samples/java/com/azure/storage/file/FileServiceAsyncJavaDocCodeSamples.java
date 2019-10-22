// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link FileServiceClient} and {@link FileServiceAsyncClient}.
 */
public class FileServiceAsyncJavaDocCodeSamples {

    /**
     * Generates code sample for {@link FileServiceAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.instantiation
        ShareAsyncClient client = new ShareClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildAsyncClient();
        // END: com.azure.storage.file.fileServiceAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link FileServiceAsyncClient} with SAS token.
     * @return An instance of {@link FileServiceAsyncClient}
     */
    public FileServiceAsyncClient createAsyncClientWithSasToken() {
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.instantiation.sastoken
        FileServiceAsyncClient fileServiceAsyncClient = new FileServiceClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .buildAsyncClient();
        // END: com.azure.storage.file.fileServiceAsyncClient.instantiation.sastoken
        return fileServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileServiceAsyncClient} with SAS token.
     * @return An instance of {@link FileServiceAsyncClient}
     */
    public FileServiceAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.instantiation.credential
        FileServiceAsyncClient fileServiceAsyncClient = new FileServiceClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .buildAsyncClient();
        // END: com.azure.storage.file.fileServiceAsyncClient.instantiation.credential
        return fileServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileServiceAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileServiceAsyncClient}
     */
    public FileServiceAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        FileServiceAsyncClient fileServiceAsyncClient = new FileServiceClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.file.fileServiceAsyncClient.instantiation.connectionstring
        return fileServiceAsyncClient;
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#createShare(String)}
     */
    public void createShareAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.createShare#string
        fileServiceAsyncClient.createShare("myshare").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.createShare#string
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#createShareWithResponse(String, Map, Integer)} with metadata
     */
    public void createShareWithResponse() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.createShareWithResponse#string-map-integer.metadata
        fileServiceAsyncClient.createShareWithResponse("test", Collections.singletonMap("share", "metadata"), null)
            .subscribe(
                response -> System.out.printf("Creating the share completed with status code %d", response.getStatusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the share!")
            );
        // END: com.azure.storage.file.fileServiceAsyncClient.createShareWithResponse#string-map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#createShareWithResponse(String, Map, Integer)} with quota.
     */
    public void createShareAsyncWithQuota() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.createShareWithResponse#string-map-integer.quota
        fileServiceAsyncClient.createShareWithResponse("test", null, 10)
            .subscribe(
                response -> System.out.printf("Creating the share completed with status code %d",
                    response.getStatusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the share!")
            );
        // END: com.azure.storage.file.fileServiceAsyncClient.createShareWithResponse#string-map-integer.quota
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#listShares()}
     */
    public void listSharesAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.listShares
        fileServiceAsyncClient.listShares().subscribe(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.listShares
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#listShares(ListSharesOptions)} of prefix.
     */
    public void listSharesAsyncWithPrefix() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.prefix
        fileServiceAsyncClient.listShares(new ListSharesOptions().setPrefix("azure")).subscribe(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.prefix
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#listShares(ListSharesOptions)} of metadata and snapshot.
     */
    public void listSharesAsyncWithOverload() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot
        fileServiceAsyncClient.listShares(new ListSharesOptions().setIncludeMetadata(true).setIncludeSnapshots(true))
            .subscribe(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete listing the shares!")
            );
        // END: com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#deleteShare(String)}
     */
    public void deleteShareAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.deleteShare#string
        fileServiceAsyncClient.deleteShare("test").doOnSuccess(
            response -> System.out.println("Deleting the share completed.")
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.deleteShare#string
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#deleteShareWithResponse(String, String)}
     */
    public void deleteShareAsyncMaxOverload() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.deleteShareWithResponse#string-string
        OffsetDateTime midnight = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        fileServiceAsyncClient.deleteShareWithResponse("test", midnight.toString())
            .subscribe(response -> System.out.printf("Deleting the snapshot completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.fileServiceAsyncClient.deleteShareWithResponse#string-string
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.getProperties
        fileServiceAsyncClient.getProperties()
            .subscribe(properties -> {
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.getHourMetrics().isEnabled(), properties.getMinuteMetrics().isEnabled());
            });
        // END: com.azure.storage.file.fileServiceAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.getPropertiesWithResponse
        fileServiceAsyncClient.getPropertiesWithResponse()
            .subscribe(properties -> System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                properties.getValue().getHourMetrics().isEnabled(),
                properties.getValue().getMinuteMetrics().isEnabled()));
        // END: com.azure.storage.file.fileServiceAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#setProperties(FileServiceProperties)}
     */
    public void setPropertiesAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.setProperties#fileServiceProperties
        fileServiceAsyncClient.getProperties().subscribe(properties -> {
            properties.getMinuteMetrics().setEnabled(true);
            properties.getHourMetrics().setEnabled(true);

            fileServiceAsyncClient.setProperties(properties)
                .subscribe(r -> System.out.println("Setting File service properties completed."));
        });
        // END: com.azure.storage.file.fileServiceAsyncClient.setProperties#fileServiceProperties
    }


    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#setPropertiesWithResponse(FileServiceProperties)}
     */
    public void setPropertiesWithResponseAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.setPropertiesWithResponseAsync#fileServiceProperties
        fileServiceAsyncClient.getPropertiesWithResponse().subscribe(response -> {
            FileServiceProperties properties = response.getValue();
            properties.getMinuteMetrics().setEnabled(true);
            properties.getHourMetrics().setEnabled(true);

            fileServiceAsyncClient.setPropertiesWithResponse(properties).subscribe(r ->
                System.out.printf("Setting File service properties completed with status code %d", r.getStatusCode()));
        });
        // END: com.azure.storage.file.fileServiceAsyncClient.setPropertiesWithResponseAsync#fileServiceProperties
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#setPropertiesWithResponse(FileServiceProperties)} to clear CORS in file service.
     */
    public void clearPropertiesAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSasToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.setPropertiesWithResponse#fileServiceProperties.clearCORS
        fileServiceAsyncClient.getProperties().subscribe(properties -> {
            properties.setCors(Collections.emptyList());

            fileServiceAsyncClient.setPropertiesWithResponse(properties).subscribe(response ->
                System.out.printf("Setting File service properties completed with status code %d",
                    response.getStatusCode()));
        });
        // END: com.azure.storage.file.fileServiceAsyncClient.setPropertiesWithResponse#fileServiceProperties.clearCORS
    }
}
