// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
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
public class ShareServiceJavaDocCodeSamples {

    private String key1 = "key1";
    private String value1 = "val1";

    /**
     * Generates code sample for {@link ShareServiceClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.instantiation
        ShareServiceClient client = new ShareServiceClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildClient();
        // END: com.azure.storage.file.share.ShareServiceClient.instantiation
    }

    /**
     * Generates code sample for {@link ShareServiceAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: 'com.azure.storage.file.share.ShareServiceAsyncClient.instantiation
        ShareServiceAsyncClient client = new ShareServiceClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildAsyncClient();
        // END: 'com.azure.storage.file.share.ShareServiceAsyncClient.instantiation
    }
    /**
     * Generates code sample for creating a {@link ShareServiceClient} with SAS token.
     * @return An instance of {@link ShareServiceClient}
     */
    public ShareServiceClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.instantiation.sastoken
        ShareServiceClient fileServiceClient = new ShareServiceClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.file.share.ShareServiceClient.instantiation.sastoken
        return fileServiceClient;
    }

    /**
     * Generates code sample for creating a {@link ShareServiceClient} with SAS token.
     * {@code SASTokenQueryParams} is composed of the Key
     * @return An instance of {@link ShareServiceClient}
     */
    public ShareServiceClient createClientWithCredential() {
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.instantiation.credential
        ShareServiceClient fileServiceClient = new ShareServiceClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .buildClient();
        // END: com.azure.storage.file.share.ShareServiceClient.instantiation.credential
        return fileServiceClient;
    }

    /**
     * Generates code sample for creating a {@link ShareServiceClient} with {@code connectionString} which turns into {@link StorageSharedKeyCredential}
     * @return An instance of {@link ShareServiceClient}
     */
    public ShareServiceClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        ShareServiceClient fileServiceClient = new ShareServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.file.share.ShareServiceClient.instantiation.connectionstring
        return fileServiceClient;
    }


    /**
     * Generates a code sample for using {@link ShareServiceClient#createShare(String)}
     */
    public void createShare() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.createShare#string
        fileServiceClient.createShare("myshare");
        System.out.println("Creating the share completed.");
        // END: com.azure.storage.file.share.ShareServiceClient.createShare#string
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#createShareWithResponse(String, Map, Integer,
     * Duration, Context)} with metadata
     */
    public void createShareWithMetadata() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: ShareServiceClient.createShareWithResponse#string-map-integer-duration-context
        Response<ShareClient> response = fileServiceClient.createShareWithResponse("test",
            Collections.singletonMap("share", "metadata"), null, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Creating the share completed with status code %d", response.getStatusCode());
        // END: ShareServiceClient.createShareWithResponse#string-map-integer-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#listShares()}
     */
    public void listShares() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.listShares
        fileServiceClient.listShares().forEach(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName())
        );
        // END: com.azure.storage.file.share.ShareServiceClient.listShares
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#listShares(ListSharesOptions, Duration, Context)} of prefix.
     */
    public void listSharesWithPrefix() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: ShareServiceClient.listShares#ListSharesOptions-Duration-Context1
        fileServiceClient.listShares(new ListSharesOptions().setPrefix("azure"), Duration.ofSeconds(1),
            new Context(key1, value1)).forEach(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName())
        );
        // END: ShareServiceClient.listShares#ListSharesOptions-Duration-Context1
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#listShares(ListSharesOptions, Duration, Context)}
     * of metadata and snapshot.
     */
    public void listSharesWithMetadataAndSnapshot() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: ShareServiceClient.listShares#ListSharesOptions-Duration-Context2
        fileServiceClient.listShares(new ListSharesOptions().setIncludeMetadata(true)
            .setIncludeSnapshots(true), Duration.ofSeconds(1), new Context(key1, value1)).forEach(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName())
        );
        // END: ShareServiceClient.listShares#ListSharesOptions-Duration-Context2
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#deleteShare(String)}
     */
    public void deleteShare() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.deleteShare#string
        fileServiceClient.deleteShare("myshare");
        // END: com.azure.storage.file.share.ShareServiceClient.deleteShare#string
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#deleteShareWithResponse(String, String,
     * Duration, Context)}
     */
    public void deleteShareMaxOverload() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.deleteShareWithResponse#string-string-duration-context
        OffsetDateTime midnight = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        Response<Void> response = fileServiceClient.deleteShareWithResponse("test", midnight.toString(),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Deleting the snapshot completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareServiceClient.deleteShareWithResponse#string-string-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#getProperties()}
     */
    public void getProperties() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.getProperties
        ShareServiceProperties properties = fileServiceClient.getProperties();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b", properties.getHourMetrics().isEnabled(),
            properties.getMinuteMetrics().isEnabled());
        // END: com.azure.storage.file.share.ShareServiceClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.getPropertiesWithResponse#duration-context
        ShareServiceProperties properties = fileServiceClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1)).getValue();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b", properties.getHourMetrics().isEnabled(),
            properties.getMinuteMetrics().isEnabled());
        // END: com.azure.storage.file.share.ShareServiceClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#setProperties(ShareServiceProperties)}
     */
    public void setProperties() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.setProperties#fileServiceProperties
        ShareServiceProperties properties = fileServiceClient.getProperties();

        properties.getMinuteMetrics().setEnabled(true);
        properties.getHourMetrics().setEnabled(true);

        fileServiceClient.setProperties(properties);
        System.out.println("Setting File service properties completed.");
        // END: com.azure.storage.file.share.ShareServiceClient.setProperties#fileServiceProperties
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#setProperties(ShareServiceProperties)}
     */
    public void setPropertiesWithResponse() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context
        ShareServiceProperties properties = fileServiceClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1)).getValue();

        properties.getMinuteMetrics().setEnabled(true);
        properties.getHourMetrics().setEnabled(true);

        Response<Void> response = fileServiceClient.setPropertiesWithResponse(properties,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting File service properties completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context
    }

    /**
     * Generates a code sample for using {@link ShareServiceClient#setProperties(ShareServiceProperties)} to clear CORS in file service.
     */
    public void clearProperties() {
        ShareServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS
        ShareServiceProperties properties = fileServiceClient.getProperties();
        properties.setCors(Collections.emptyList());

        Response<Void> response = fileServiceClient.setPropertiesWithResponse(properties,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting File service properties completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS
    }

    /**
     * Code snippet for {@link ShareServiceClient#generateAccountSas(AccountSasSignatureValues)}
     */
    public void generateAccountSas() {
        ShareServiceClient fileServiceClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareServiceClient.generateAccountSas#AccountSasSignatureValues
        AccountSasPermission permissions = new AccountSasPermission()
            .setListPermission(true)
            .setReadPermission(true);
        AccountSasResourceType resourceTypes = new AccountSasResourceType().setContainer(true);
        AccountSasService services = new AccountSasService().setBlobAccess(true).setFileAccess(true);
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(Duration.ofDays(2));

        AccountSasSignatureValues sasValues =
            new AccountSasSignatureValues(expiryTime, permissions, services, resourceTypes);

        // Client must be authenticated via StorageSharedKeyCredential
        String sas = fileServiceClient.generateAccountSas(sasValues);
        // END: com.azure.storage.file.share.ShareServiceClient.generateAccountSas#AccountSasSignatureValues
    }
}
