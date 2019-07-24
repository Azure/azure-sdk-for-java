// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link FileStorageClient} and {@link FileStorageAsyncClient}.
 */
public class FileStorageJavaDocCodeSamples {
    /**
     * Generates code sample for {@link FileStorageClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.fileStorageClient.instantiation
        FileStorageClient client = new FileStorageClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildClient();
        // END: com.azure.storage.file.fileStorageClient.instantiation
    }

    /**
     * Generates code sample for {@link FileStorageAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.instantiation
        ShareAsyncClient client = new ShareClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildAsyncClient();
        // END: com.azure.storage.file.fileStorageAsyncClient.instantiation
    }
    /**
     * Generates code sample for creating a {@link FileStorageClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileStorageClient}
     */
    public FileStorageClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.file.fileStorageClient.instantiation.sastoken
        FileStorageClient fileStorageClient = new FileStorageClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.file.fileStorageClient.instantiation.sastoken
        return fileStorageClient;
    }

    /**
     * Generates code sample for creating a {@link FileStorageAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileStorageAsyncClient}
     */
    public FileStorageAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.instantiation.sastoken
        FileStorageAsyncClient fileStorageAsyncClient = new FileStorageClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .buildAsyncClient();
        // END: com.azure.storage.file.fileStorageAsyncClient.instantiation.sastoken
        return fileStorageAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileStorageClient} with {@link SASTokenCredential}
     * {@code SASTokenQueryParams} is composed of the Key
     * @return An instance of {@link FileStorageClient}
     */
    public FileStorageClient createClientWithCredential() {
        // BEGIN: com.azure.storage.file.fileStorageClient.instantiation.credential
        FileStorageClient fileStorageClient = new FileStorageClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .buildClient();
        // END: com.azure.storage.file.fileStorageClient.instantiation.credential
        return fileStorageClient;
    }

    /**
     * Generates code sample for creating a {@link FileStorageAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileStorageAsyncClient}
     */
    public FileStorageAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.instantiation.credential
        FileStorageAsyncClient fileStorageAsyncClient = new FileStorageClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .buildAsyncClient();
        // END: com.azure.storage.file.fileStorageAsyncClient.instantiation.credential
        return fileStorageAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileStorageClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileStorageClient}
     */
    public FileStorageClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileStorageClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        FileStorageClient fileStorageClient = new FileStorageClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.file.fileStorageClient.instantiation.connectionstring
        return fileStorageClient;
    }

    /**
     * Generates code sample for creating a {@link FileStorageAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileStorageAsyncClient}
     */
    public FileStorageAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        FileStorageAsyncClient fileStorageAsyncClient = new FileStorageClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.file.fileStorageAsyncClient.instantiation.connectionstring
        return fileStorageAsyncClient;
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#createShare(String)}
     */
    public void createShare() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.createShare#string
        Response<ShareClient> response = fileStorageClient.createShare("myshare");
        System.out.printf("Creating the share completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileStorageClient.createShare#string
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#createShare(String)}
     */
    public void createShareAsync() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.createShare#string
        fileStorageAsyncClient.createShare("myshare").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.fileStorageAsyncClient.createShare#string
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#createShare(String, Map, Integer)} with metadata
     */
    public void createShareWithMetadata() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.createShare#string-map-integer.metadata
        Response<ShareClient> response = fileStorageClient.createShare("test",
            Collections.singletonMap("share", "metadata"), null);
        System.out.printf("Creating the share completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileStorageClient.createShare#string-map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#createShare(String, Map, Integer)} with metadata
     */
    public void createShareAsyncWithMetadata() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.createShare#string-map-integer.metadata
        fileStorageAsyncClient.createShare("test", Collections.singletonMap("share", "metadata"), null)
            .subscribe(
                response -> System.out.printf("Creating the share completed with status code %d", response.statusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the share!")
            );
        // END: com.azure.storage.file.fileStorageAsyncClient.createShare#string-map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#createShare(String, Map, Integer)} with quota.
     */
    public void createShareWithQuota() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.createShare#string-map-integer.quota
        Response<ShareClient> response = fileStorageClient.createShare("test", null, 10);
        System.out.printf("Creating the share completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileStorageClient.createShare#string-map-integer.quota
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#createShare(String, Map, Integer)} with quota.
     */
    public void createShareAsyncWithQuota() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.createShare#string-map-integer.quota
        fileStorageAsyncClient.createShare("test", null, 10)
            .subscribe(
                response -> System.out.printf("Creating the share completed with status code %d",
                    response.statusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the share!")
            );
        // END: com.azure.storage.file.fileStorageAsyncClient.createShare#string-map-integer.quota
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#listShares()}
     */
    public void listShares() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.listShares
        fileStorageClient.listShares().forEach(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.name())
        );
        // END: com.azure.storage.file.fileStorageClient.listShares
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#listShares()}
     */
    public void listSharesAsync() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.listShares
        fileStorageAsyncClient.listShares().subscribe(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.name()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.fileStorageAsyncClient.listShares
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#listShares(ListSharesOptions)} of prefix.
     */
    public void listSharesWithPrefix() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.listShares#ListSharesOptions.prefix
        fileStorageClient.listShares(new ListSharesOptions().prefix("azure")).forEach(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.name())
        );
        // END: com.azure.storage.file.fileStorageClient.listShares#ListSharesOptions.prefix
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#listShares(ListSharesOptions)} of prefix.
     */
    public void listSharesAsyncWithPrefix() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.listShares#ListSharesOptions.prefix
        fileStorageAsyncClient.listShares(new ListSharesOptions().prefix("azure")).subscribe(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.name()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.fileStorageAsyncClient.listShares#ListSharesOptions.prefix
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#listShares(ListSharesOptions)} of metadata and snapshot.
     */
    public void listSharesWithMetadataAndSnapshot() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.listShares#ListSharesOptions.metadata.snapshot
        fileStorageClient.listShares(new ListSharesOptions().includeMetadata(true)
            .includeSnapshots(true)).forEach(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.name())
        );
        // END: com.azure.storage.file.fileStorageClient.listShares#ListSharesOptions.metadata.snapshot
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#listShares(ListSharesOptions)} of metadata and snapshot.
     */
    public void listSharesAsyncWithOverload() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.listShares#ListSharesOptions.metadata.snapshot
        fileStorageAsyncClient.listShares(new ListSharesOptions().includeMetadata(true)
            .includeSnapshots(true)).subscribe(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.name()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.fileStorageAsyncClient.listShares#ListSharesOptions.metadata.snapshot
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#deleteShare(String)}
     */
    public void deleteShare() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.deleteShare#string
        fileStorageClient.deleteShare("myshare");
        // END: com.azure.storage.file.fileStorageClient.deleteShare#string
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#deleteShare(String)}
     */
    public void deleteShareAsync() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.deleteShare#string
        fileStorageAsyncClient.deleteShare("test").subscribe(
            response -> System.out.println("Deleting the share completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.file.fileStorageAsyncClient.deleteShare#string
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#deleteShare(String, String)}
     */
    public void deleteShareMaxOverload() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.deleteShare#string-string
        OffsetDateTime midnight = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        VoidResponse response = fileStorageClient.deleteShare("test", midnight.toString());
        System.out.printf("Deleting the snapshot completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileStorageClient.deleteShare#string-string
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#deleteShare(String, String)}
     */
    public void deleteShareAsyncMaxOverload() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.deleteShare#string-string
        OffsetDateTime midnight = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        fileStorageAsyncClient.deleteShare("test", midnight.toString())
            .subscribe(response -> System.out.printf("Deleting the snapshot completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileStorageAsyncClient.deleteShare#string-string
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#getProperties()}
     */
    public void getProperties() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.getProperties
        FileServiceProperties properties = fileStorageClient.getProperties().value();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b", properties.hourMetrics().enabled(),
            properties.minuteMetrics().enabled());
        // END: com.azure.storage.file.fileStorageClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.getProperties
        fileStorageAsyncClient.getProperties()
            .subscribe(response -> {
                FileServiceProperties properties = response.value();
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
            });
        // END: com.azure.storage.file.fileStorageAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#setProperties(FileServiceProperties)}
     */
    public void setProperties() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.setProperties#fileServiceProperties
        FileServiceProperties properties = fileStorageClient.getProperties().value();

        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);

        VoidResponse response = fileStorageClient.setProperties(properties);
        System.out.printf("Setting File service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileStorageClient.setProperties#fileServiceProperties
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#setProperties(FileServiceProperties)}
     */
    public void setPropertiesAsync() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.setProperties#fileServiceProperties
        FileServiceProperties properties = fileStorageAsyncClient.getProperties().block().value();

        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);

        fileStorageAsyncClient.setProperties(properties)
            .subscribe(response -> System.out.printf("Setting File service properties completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileStorageAsyncClient.setProperties#fileServiceProperties
    }

    /**
     * Generates a code sample for using {@link FileStorageClient#setProperties(FileServiceProperties)} to clear CORS in file service.
     */
    public void clearProperties() {
        FileStorageClient fileStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageClient.setProperties#fileServiceProperties.clearCORS
        FileServiceProperties properties = fileStorageClient.getProperties().value();
        properties.cors(Collections.emptyList());

        VoidResponse response = fileStorageClient.setProperties(properties);
        System.out.printf("Setting File service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileStorageClient.setProperties#fileServiceProperties.clearCORS
    }

    /**
     * Generates a code sample for using {@link FileStorageAsyncClient#setProperties(FileServiceProperties)} to clear CORS in file service.
     */
    public void clearPropertiesAsync() {
        FileStorageAsyncClient fileStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileStorageAsyncClient.setProperties#fileServiceProperties.clearCORS
        FileServiceProperties properties = fileStorageAsyncClient.getProperties().block().value();
        properties.cors(Collections.emptyList());

        fileStorageAsyncClient.setProperties(properties)
            .subscribe(response -> System.out.printf("Setting File service properties completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileStorageAsyncClient.setProperties#fileServiceProperties.clearCORS
    }
}
