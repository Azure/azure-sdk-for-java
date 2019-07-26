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
 * Contains code snippets when generating javadocs through doclets for {@link FileServiceClient} and {@link FileServiceAsyncClient}.
 */
public class FileServiceJavaDocCodeSamples {
    /**
     * Generates code sample for {@link FileServiceClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.fileServiceClient.instantiation
        FileServiceClient client = new FileServiceClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildClient();
        // END: com.azure.storage.file.fileServiceClient.instantiation
    }

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
     * Generates code sample for creating a {@link FileServiceClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileServiceClient}
     */
    public FileServiceClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.file.fileServiceClient.instantiation.sastoken
        FileServiceClient fileServiceClient = new FileServiceClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.file.fileServiceClient.instantiation.sastoken
        return fileServiceClient;
    }

    /**
     * Generates code sample for creating a {@link FileServiceAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileServiceAsyncClient}
     */
    public FileServiceAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.instantiation.sastoken
        FileServiceAsyncClient fileServiceAsyncClient = new FileServiceClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .buildAsyncClient();
        // END: com.azure.storage.file.fileServiceAsyncClient.instantiation.sastoken
        return fileServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileServiceClient} with {@link SASTokenCredential}
     * {@code SASTokenQueryParams} is composed of the Key
     * @return An instance of {@link FileServiceClient}
     */
    public FileServiceClient createClientWithCredential() {
        // BEGIN: com.azure.storage.file.fileServiceClient.instantiation.credential
        FileServiceClient fileServiceClient = new FileServiceClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .buildClient();
        // END: com.azure.storage.file.fileServiceClient.instantiation.credential
        return fileServiceClient;
    }

    /**
     * Generates code sample for creating a {@link FileServiceAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileServiceAsyncClient}
     */
    public FileServiceAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.instantiation.credential
        FileServiceAsyncClient fileServiceAsyncClient = new FileServiceClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .buildAsyncClient();
        // END: com.azure.storage.file.fileServiceAsyncClient.instantiation.credential
        return fileServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileServiceClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileServiceClient}
     */
    public FileServiceClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileServiceClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        FileServiceClient fileServiceClient = new FileServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.file.fileServiceClient.instantiation.connectionstring
        return fileServiceClient;
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
     * Generates a code sample for using {@link FileServiceClient#createShare(String)}
     */
    public void createShare() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.createShare#string
        Response<ShareClient> response = fileServiceClient.createShare("myshare");
        System.out.printf("Creating the share completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileServiceClient.createShare#string
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#createShare(String)}
     */
    public void createShareAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.createShare#string
        fileServiceAsyncClient.createShare("myshare").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.createShare#string
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#createShare(String, Map, Integer)} with metadata
     */
    public void createShareWithMetadata() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.createShare#string-map-integer.metadata
        Response<ShareClient> response = fileServiceClient.createShare("test",
            Collections.singletonMap("share", "metadata"), null);
        System.out.printf("Creating the share completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileServiceClient.createShare#string-map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#createShare(String, Map, Integer)} with metadata
     */
    public void createShareAsyncWithMetadata() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.createShare#string-map-integer.metadata
        fileServiceAsyncClient.createShare("test", Collections.singletonMap("share", "metadata"), null)
            .subscribe(
                response -> System.out.printf("Creating the share completed with status code %d", response.statusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the share!")
            );
        // END: com.azure.storage.file.fileServiceAsyncClient.createShare#string-map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#createShare(String, Map, Integer)} with quota.
     */
    public void createShareWithQuota() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.createShare#string-map-integer.quota
        Response<ShareClient> response = fileServiceClient.createShare("test", null, 10);
        System.out.printf("Creating the share completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileServiceClient.createShare#string-map-integer.quota
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#createShare(String, Map, Integer)} with quota.
     */
    public void createShareAsyncWithQuota() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.createShare#string-map-integer.quota
        fileServiceAsyncClient.createShare("test", null, 10)
            .subscribe(
                response -> System.out.printf("Creating the share completed with status code %d",
                    response.statusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the share!")
            );
        // END: com.azure.storage.file.fileServiceAsyncClient.createShare#string-map-integer.quota
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
     * Generates a code sample for using {@link FileServiceClient#listShares(ListSharesOptions)} of prefix.
     */
    public void listSharesWithPrefix() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.listShares#ListSharesOptions.prefix
        fileServiceClient.listShares(new ListSharesOptions().prefix("azure")).forEach(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.name())
        );
        // END: com.azure.storage.file.fileServiceClient.listShares#ListSharesOptions.prefix
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#listShares(ListSharesOptions)} of prefix.
     */
    public void listSharesAsyncWithPrefix() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.prefix
        fileServiceAsyncClient.listShares(new ListSharesOptions().prefix("azure")).subscribe(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.name()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.prefix
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#listShares(ListSharesOptions)} of metadata and snapshot.
     */
    public void listSharesWithMetadataAndSnapshot() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.listShares#ListSharesOptions.metadata.snapshot
        fileServiceClient.listShares(new ListSharesOptions().includeMetadata(true)
            .includeSnapshots(true)).forEach(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.name())
        );
        // END: com.azure.storage.file.fileServiceClient.listShares#ListSharesOptions.metadata.snapshot
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#listShares(ListSharesOptions)} of metadata and snapshot.
     */
    public void listSharesAsyncWithOverload() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot
        fileServiceAsyncClient.listShares(new ListSharesOptions().includeMetadata(true)
            .includeSnapshots(true)).subscribe(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.name()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot
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
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.deleteShare#string
        fileServiceAsyncClient.deleteShare("test").subscribe(
            response -> System.out.println("Deleting the share completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.file.fileServiceAsyncClient.deleteShare#string
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#deleteShare(String, String)}
     */
    public void deleteShareMaxOverload() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.deleteShare#string-string
        OffsetDateTime midnight = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        VoidResponse response = fileServiceClient.deleteShare("test", midnight.toString());
        System.out.printf("Deleting the snapshot completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileServiceClient.deleteShare#string-string
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#deleteShare(String, String)}
     */
    public void deleteShareAsyncMaxOverload() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.deleteShare#string-string
        OffsetDateTime midnight = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        fileServiceAsyncClient.deleteShare("test", midnight.toString())
            .subscribe(response -> System.out.printf("Deleting the snapshot completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileServiceAsyncClient.deleteShare#string-string
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#getProperties()}
     */
    public void getProperties() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.getProperties
        FileServiceProperties properties = fileServiceClient.getProperties().value();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b", properties.hourMetrics().enabled(),
            properties.minuteMetrics().enabled());
        // END: com.azure.storage.file.fileServiceClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.getProperties
        fileServiceAsyncClient.getProperties()
            .subscribe(response -> {
                FileServiceProperties properties = response.value();
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
            });
        // END: com.azure.storage.file.fileServiceAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#setProperties(FileServiceProperties)}
     */
    public void setProperties() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.setProperties#fileServiceProperties
        FileServiceProperties properties = fileServiceClient.getProperties().value();

        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);

        VoidResponse response = fileServiceClient.setProperties(properties);
        System.out.printf("Setting File service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileServiceClient.setProperties#fileServiceProperties
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#setProperties(FileServiceProperties)}
     */
    public void setPropertiesAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.setProperties#fileServiceProperties
        FileServiceProperties properties = fileServiceAsyncClient.getProperties().block().value();

        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);

        fileServiceAsyncClient.setProperties(properties)
            .subscribe(response -> System.out.printf("Setting File service properties completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileServiceAsyncClient.setProperties#fileServiceProperties
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#setProperties(FileServiceProperties)} to clear CORS in file service.
     */
    public void clearProperties() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.setProperties#fileServiceProperties.clearCORS
        FileServiceProperties properties = fileServiceClient.getProperties().value();
        properties.cors(Collections.emptyList());

        VoidResponse response = fileServiceClient.setProperties(properties);
        System.out.printf("Setting File service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileServiceClient.setProperties#fileServiceProperties.clearCORS
    }

    /**
     * Generates a code sample for using {@link FileServiceAsyncClient#setProperties(FileServiceProperties)} to clear CORS in file service.
     */
    public void clearPropertiesAsync() {
        FileServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceAsyncClient.setProperties#fileServiceProperties.clearCORS
        FileServiceProperties properties = fileServiceAsyncClient.getProperties().block().value();
        properties.cors(Collections.emptyList());

        fileServiceAsyncClient.setProperties(properties)
            .subscribe(response -> System.out.printf("Setting File service properties completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileServiceAsyncClient.setProperties#fileServiceProperties.clearCORS
    }
}
