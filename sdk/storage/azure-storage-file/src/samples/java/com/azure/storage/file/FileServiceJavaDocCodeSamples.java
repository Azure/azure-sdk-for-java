// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.core.util.Context;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.ListSharesOptions;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link FileServiceClient} and {@link FileServiceAsyncClient}.
 */
public class FileServiceJavaDocCodeSamples {

    private String key1 = "key1";
    private String value1 = "val1";

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
     * Generates code sample for creating a {@link FileServiceClient} with {@link SASTokenCredential}
     * {@code SASTokenQueryParams} is composed of the Key
     * @return An instance of {@link FileServiceClient}
     */
    public FileServiceClient createClientWithCredential() {
        // BEGIN: com.azure.storage.file.fileServiceClient.instantiation.credential
        FileServiceClient fileServiceClient = new FileServiceClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("${SASTokenQueryParams}")))
            .buildClient();
        // END: com.azure.storage.file.fileServiceClient.instantiation.credential
        return fileServiceClient;
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
     * Generates a code sample for using {@link FileServiceClient#createShare(String)}
     */
    public void createShare() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.createShare#string
        fileServiceClient.createShare("myshare");
        System.out.println("Creating the share completed.");
        // END: com.azure.storage.file.fileServiceClient.createShare#string
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#createShareWithResponse(String, Map, Integer,
     * Duration, Context)} with metadata
     */
    public void createShareWithMetadata() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileServiceClient.createShareWithResponse#string-map-integer-duration-context
        Response<ShareClient> response = fileServiceClient.createShareWithResponse("test",
            Collections.singletonMap("share", "metadata"), null, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Creating the share completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.FileServiceClient.createShareWithResponse#string-map-integer-duration-context
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#listShares()}
     */
    public void listShares() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.listShares
        fileServiceClient.listShares().forEach(
            shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName())
        );
        // END: com.azure.storage.file.fileServiceClient.listShares
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#listShares(ListSharesOptions, Duration, Context)} of prefix.
     */
    public void listSharesWithPrefix() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileServiceClient.listShares#ListSharesOptions-Duration-Context1
        fileServiceClient.listShares(new ListSharesOptions().setPrefix("azure"), Duration.ofSeconds(1),
            new Context(key1, value1)).forEach(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName())
        );
        // END: com.azure.storage.file.FileServiceClient.listShares#ListSharesOptions-Duration-Context1
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#listShares(ListSharesOptions, Duration, Context)}
     * of metadata and snapshot.
     */
    public void listSharesWithMetadataAndSnapshot() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileServiceClient.listShares#ListSharesOptions-Duration-Context2
        fileServiceClient.listShares(new ListSharesOptions().setIncludeMetadata(true)
            .setIncludeSnapshots(true), Duration.ofSeconds(1), new Context(key1, value1)).forEach(
                shareItem -> System.out.printf("Share %s exists in the account", shareItem.getName())
        );
        // END: com.azure.storage.file.FileServiceClient.listShares#ListSharesOptions-Duration-Context2
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
     * Generates a code sample for using {@link FileServiceClient#deleteShareWithResponse(String, String,
     * Duration, Context)}
     */
    public void deleteShareMaxOverload() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.deleteShareWithResponse#string-string-duration-context
        OffsetDateTime midnight = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        Response<Void> response = fileServiceClient.deleteShareWithResponse("test", midnight.toString(),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Deleting the snapshot completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.fileServiceClient.deleteShareWithResponse#string-string-duration-context
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#getProperties()}
     */
    public void getProperties() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.getProperties
        FileServiceProperties properties = fileServiceClient.getProperties();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b", properties.getHourMetrics().isEnabled(),
            properties.getMinuteMetrics().isEnabled());
        // END: com.azure.storage.file.fileServiceClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.getPropertiesWithResponse#duration-context
        FileServiceProperties properties = fileServiceClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1)).getValue();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b", properties.getHourMetrics().isEnabled(),
            properties.getMinuteMetrics().isEnabled());
        // END: com.azure.storage.file.fileServiceClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#setProperties(FileServiceProperties)}
     */
    public void setProperties() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.setProperties#fileServiceProperties
        FileServiceProperties properties = fileServiceClient.getProperties();

        properties.getMinuteMetrics().setEnabled(true);
        properties.getHourMetrics().setEnabled(true);

        fileServiceClient.setProperties(properties);
        System.out.println("Setting File service properties completed.");
        // END: com.azure.storage.file.fileServiceClient.setProperties#fileServiceProperties
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#setProperties(FileServiceProperties)}
     */
    public void setPropertiesWithResponse() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context
        FileServiceProperties properties = fileServiceClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1)).getValue();

        properties.getMinuteMetrics().setEnabled(true);
        properties.getHourMetrics().setEnabled(true);

        Response<Void> response = fileServiceClient.setPropertiesWithResponse(properties,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting File service properties completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#setProperties(FileServiceProperties)} to clear CORS in file service.
     */
    public void clearProperties() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS
        FileServiceProperties properties = fileServiceClient.getProperties();
        properties.setCors(Collections.emptyList());

        Response<Void> response = fileServiceClient.setPropertiesWithResponse(properties,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting File service properties completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.fileServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS
    }

    /**
     * Generates a code sample for using {@link FileServiceClient#generateAccountSAS(AccountSASService,
     * AccountSASResourceType, AccountSASPermission, OffsetDateTime, OffsetDateTime, String, IPRange, SASProtocol)}
     */
    public void generateAccountSAS() {
        FileServiceClient fileServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileServiceClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
        AccountSASService service = new AccountSASService()
            .setBlob(true)
            .setFile(true)
            .setQueue(true)
            .setTable(true);
        AccountSASResourceType resourceType = new AccountSASResourceType()
            .setContainer(true)
            .setObject(true)
            .setService(true);
        AccountSASPermission permission = new AccountSASPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setCreatePermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setListPermission(true)
            .setProcessMessages(true)
            .setUpdatePermission(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        String sas = fileServiceClient.generateAccountSAS(service, resourceType, permission, expiryTime, startTime,
            version, ipRange, sasProtocol);
        // END: com.azure.storage.file.FileServiceClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
    }


}
