// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.azure.storage.file.share.models.ListSharesOptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ShareServiceClient} and {@link ShareServiceAsyncClient}.
 */
public class ShareServiceAsyncJavaDocCodeSamples {

    /**
     * Generates code sample for {@link ShareServiceAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.instantiation
        ShareAsyncClient client = new ShareClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildAsyncClient();
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link ShareServiceAsyncClient} with SAS token.
     * @return An instance of {@link ShareServiceAsyncClient}
     */
    public ShareServiceAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.instantiation.sastoken
        ShareServiceAsyncClient fileServiceAsyncClient = new ShareServiceClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .buildAsyncClient();
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.instantiation.sastoken
        return fileServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareServiceAsyncClient} with SAS token.
     * @return An instance of {@link ShareServiceAsyncClient}
     */
    public ShareServiceAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.instantiation.credential
        ShareServiceAsyncClient fileServiceAsyncClient = new ShareServiceClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .buildAsyncClient();
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.instantiation.credential
        return fileServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareServiceAsyncClient} with {@code connectionString} which turns into {@link StorageSharedKeyCredential}
     * @return An instance of {@link ShareServiceAsyncClient}
     */
    public ShareServiceAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        ShareServiceAsyncClient fileServiceAsyncClient = new ShareServiceClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.instantiation.connectionstring
        return fileServiceAsyncClient;
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#createShare(String)}
     */
    public void createShareAsync() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.createShare#string
        fileServiceAsyncClient.createShare("myshare").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the share!")
        );
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.createShare#string
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#createShareWithResponse(String, Map, Integer)} with metadata
     */
    public void createShareWithResponse() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.metadata
        fileServiceAsyncClient.createShareWithResponse("test", Collections.singletonMap("share", "metadata"), null)
            .subscribe(
                response -> System.out.printf("Creating the share completed with status code %d", response.getStatusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the share!")
            );
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.metadata
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#createShareWithResponse(String, Map, Integer)} with quota.
     */
    public void createShareAsyncWithQuota() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.quota
        fileServiceAsyncClient.createShareWithResponse("test", null, 10)
            .subscribe(
                response -> System.out.printf("Creating the share completed with status code %d",
                    response.getStatusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the share!")
            );
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.quota
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#listShares()}
     */
    public void listSharesAsync() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.listShares
        fileServiceAsyncClient.listShares().subscribe(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.listShares
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#listShares(ListSharesOptions)} of prefix.
     */
    public void listSharesAsyncWithPrefix() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.prefix
        fileServiceAsyncClient.listShares(new ListSharesOptions().setPrefix("azure")).subscribe(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the shares!")
        );
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.prefix
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#listShares(ListSharesOptions)} of metadata and snapshot.
     */
    public void listSharesAsyncWithOverload() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot
        fileServiceAsyncClient.listShares(new ListSharesOptions().setIncludeMetadata(true).setIncludeSnapshots(true))
            .subscribe(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete listing the shares!")
            );
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#deleteShare(String)}
     */
    public void deleteShareAsync() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.deleteShare#string
        fileServiceAsyncClient.deleteShare("test").doOnSuccess(
            response -> System.out.println("Deleting the share completed.")
        );
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.deleteShare#string
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#deleteShareWithResponse(String, String)}
     */
    public void deleteShareAsyncMaxOverload() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.deleteShareWithResponse#string-string
        OffsetDateTime midnight = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        fileServiceAsyncClient.deleteShareWithResponse("test", midnight.toString())
            .subscribe(response -> System.out.printf("Deleting the snapshot completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.deleteShareWithResponse#string-string
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.getProperties
        fileServiceAsyncClient.getProperties()
            .subscribe(properties -> {
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.getHourMetrics().isEnabled(), properties.getMinuteMetrics().isEnabled());
            });
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.getPropertiesWithResponse
        fileServiceAsyncClient.getPropertiesWithResponse()
            .subscribe(properties -> System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                properties.getValue().getHourMetrics().isEnabled(),
                properties.getValue().getMinuteMetrics().isEnabled()));
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#setProperties(ShareServiceProperties)}
     */
    public void setPropertiesAsync() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.setProperties#fileServiceProperties
        fileServiceAsyncClient.getProperties().subscribe(properties -> {
            properties.getMinuteMetrics().setEnabled(true);
            properties.getHourMetrics().setEnabled(true);

            fileServiceAsyncClient.setProperties(properties)
                .subscribe(r -> System.out.println("Setting File service properties completed."));
        });
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.setProperties#fileServiceProperties
    }


    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#setPropertiesWithResponse(ShareServiceProperties)}
     */
    public void setPropertiesWithResponseAsync() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponseAsync#fileServiceProperties
        fileServiceAsyncClient.getPropertiesWithResponse().subscribe(response -> {
            ShareServiceProperties properties = response.getValue();
            properties.getMinuteMetrics().setEnabled(true);
            properties.getHourMetrics().setEnabled(true);

            fileServiceAsyncClient.setPropertiesWithResponse(properties).subscribe(r ->
                System.out.printf("Setting File service properties completed with status code %d", r.getStatusCode()));
        });
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponseAsync#fileServiceProperties
    }

    /**
     * Generates a code sample for using {@link ShareServiceAsyncClient#setPropertiesWithResponse(ShareServiceProperties)} to clear CORS in file service.
     */
    public void clearPropertiesAsync() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponse#fileServiceProperties.clearCORS
        fileServiceAsyncClient.getProperties().subscribe(properties -> {
            properties.setCors(Collections.emptyList());

            fileServiceAsyncClient.setPropertiesWithResponse(properties).subscribe(response ->
                System.out.printf("Setting File service properties completed with status code %d",
                    response.getStatusCode()));
        });
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponse#fileServiceProperties.clearCORS
    }

    /**
     * Code snippet for {@link ShareServiceAsyncClient#generateAccountSas(AccountSasSignatureValues)}
     */
    public void generateAccountSas() {
        ShareServiceAsyncClient fileServiceAsyncClient = createAsyncClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareServiceAsyncClient.generateAccountSas#AccountSasSignatureValues
        AccountSasPermission permissions = new AccountSasPermission()
            .setListPermission(true)
            .setReadPermission(true);
        AccountSasResourceType resourceTypes = new AccountSasResourceType().setContainer(true);
        AccountSasService services = new AccountSasService().setBlobAccess(true).setFileAccess(true);
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(Duration.ofDays(2));

        AccountSasSignatureValues sasValues =
            new AccountSasSignatureValues(expiryTime, permissions, services, resourceTypes);

        // Client must be authenticated via StorageSharedKeyCredential
        String sas = fileServiceAsyncClient.generateAccountSas(sasValues);
        // END: com.azure.storage.file.share.ShareServiceAsyncClient.generateAccountSas#AccountSasSignatureValues
    }
}
