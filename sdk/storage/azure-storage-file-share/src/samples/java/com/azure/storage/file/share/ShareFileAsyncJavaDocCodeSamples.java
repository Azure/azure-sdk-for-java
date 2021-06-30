// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.core.util.Context;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.models.DownloadRetryOptions;
import com.azure.storage.file.share.models.FileRange;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ShareFileClient} and {@link ShareFileAsyncClient}.
 */
public class ShareFileAsyncJavaDocCodeSamples {
    String leaseId = "leaseId";
    ShareFileAsyncClient client = createAsyncClientWithSASToken();

    /**
     * Generates code sample for {@link ShareFileAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.instantiation
        ShareFileAsyncClient client = new ShareFileClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildFileAsyncClient();
        // END: com.azure.storage.file.share.ShareFileAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link ShareFileAsyncClient} with SAS token.
     * @return An instance of {@link ShareFileAsyncClient}
     */
    public ShareFileAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.instantiation.sastoken
        ShareFileAsyncClient shareFileAsyncClient = new ShareFileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .shareName("myshare")
            .resourcePath("myfilepath")
            .buildFileAsyncClient();
        // END: com.azure.storage.file.share.ShareFileAsyncClient.instantiation.sastoken
        return shareFileAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareFileAsyncClient} with SAS token.
     * @return An instance of {@link ShareFileAsyncClient}
     */
    public ShareFileAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.instantiation.credential
        ShareFileAsyncClient shareFileAsyncClient = new ShareFileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .resourcePath("myfilepath")
            .buildFileAsyncClient();
        // END: com.azure.storage.file.share.ShareFileAsyncClient.instantiation.credential
        return shareFileAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareFileAsyncClient} with {@code connectionString}
     * which turns into {@link StorageSharedKeyCredential}
     * @return An instance of {@link ShareFileAsyncClient}
     */
    public ShareFileAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        ShareFileAsyncClient shareFileAsyncClient = new ShareFileClientBuilder()
            .connectionString(connectionString).shareName("myshare").resourcePath("myfilepath")
            .buildFileAsyncClient();
        // END: com.azure.storage.file.share.ShareFileAsyncClient.instantiation.connectionstring
        return shareFileAsyncClient;
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#create(long)}
     */
    public void createFileAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.create
        shareFileAsyncClient.create(1024).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the file!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.create
    }

    /**
     * Code snippet for {@link ShareFileAsyncClient#exists()}
     */
    public void exists() {
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.exists
    }

    /**
     * Code snippet for {@link ShareFileAsyncClient#existsWithResponse()}
     */
    public void existsWithResponse() {
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.existsWithResponse
        client.existsWithResponse().subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.existsWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#createWithResponse(long, ShareFileHttpHeaders, FileSmbProperties, String, Map)}
     */
    public void createWithResponse() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        shareFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"))
            .subscribe(response -> System.out.printf("Creating the file completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#createWithResponse(long, ShareFileHttpHeaders, FileSmbProperties, String, Map, ShareRequestConditions)}
     */
    public void createWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set

        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);

        shareFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"), requestConditions)
            .subscribe(response -> System.out.printf("Creating the file completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#beginCopy(String, Map, Duration)}
     */
    public void beginCopy() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-map-duration
        PollerFlux<ShareFileCopyInfo, Void> poller = shareFileAsyncClient.beginCopy(
            "https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata"), Duration.ofSeconds(2));

        poller.subscribe(response -> {
            final ShareFileCopyInfo value = response.getValue();
            System.out.printf("Copy source: %s. Status: %s.%n", value.getCopySourceUrl(), value.getCopyStatus());
        }, error -> System.err.println("Error: " + error),
            () -> System.out.println("Complete copying the file."));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-map-duration
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#beginCopy(String, FileSmbProperties, String, PermissionCopyModeType, Boolean, Boolean, Map, Duration, ShareRequestConditions)}
     */
    public void beginCopy2() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-filesmbproperties-string-permissioncopymodetype-boolean-boolean-map-duration-ShareRequestConditions
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        boolean ignoreReadOnly = false; // Default value
        boolean setArchiveAttribute = true; // Default value
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);

        PollerFlux<ShareFileCopyInfo, Void> poller = shareFileAsyncClient.beginCopy(
            "https://{accountName}.file.core.windows.net?{SASToken}",
            smbProperties, filePermission, PermissionCopyModeType.SOURCE, ignoreReadOnly, setArchiveAttribute,
            Collections.singletonMap("file", "metadata"), Duration.ofSeconds(2), requestConditions);

        poller.subscribe(response -> {
            final ShareFileCopyInfo value = response.getValue();
            System.out.printf("Copy source: %s. Status: %s.%n", value.getCopySourceUrl(), value.getCopyStatus());
        }, error -> System.err.println("Error: " + error), () -> System.out.println("Complete copying the file."));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.beginCopy#string-filesmbproperties-string-permissioncopymodetype-boolean-boolean-map-duration-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#abortCopy(String)}
     */
    public void abortCopyFileAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.abortCopy#string
        shareFileAsyncClient.abortCopy("someCopyId")
            .doOnSuccess(response -> System.out.println("Abort copying the file completed."));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.abortCopy#string
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#abortCopyWithResponse(String)}
     */
    public void abortCopyWithResponse() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string
        shareFileAsyncClient.abortCopyWithResponse("someCopyId")
            .subscribe(response -> System.out.printf("Abort copying the file completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#abortCopyWithResponse(String, ShareRequestConditions)}
     */
    public void abortCopyWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.abortCopyWithResponse("someCopyId", requestConditions)
            .subscribe(response -> System.out.printf("Abort copying the file completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.abortCopyWithResponse#string-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#upload(Flux, long)}
     */
    public void uploadDataAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.upload#flux-long
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        shareFileAsyncClient.upload(Flux.just(defaultData), defaultData.remaining()).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.upload#flux-long
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#uploadWithResponse(Flux, long, Long)}
     */
    public void uploadDataMaxOverloadAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        shareFileAsyncClient.uploadWithResponse(Flux.just(defaultData), defaultData.remaining(), 0L).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#uploadRange(Flux, long)}
     */
    public void uploadRange() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadRange#Flux-long
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        shareFileAsyncClient.uploadRange(Flux.just(defaultData), defaultData.remaining()).subscribe(
                response -> { },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadRange#Flux-long
    }

    /**
     * Generates a code sample for using
     * {@link ShareFileAsyncClient#uploadRangeWithResponse(ShareFileUploadRangeOptions)}
     */
    public void uploadRangeWithResponse() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeWithResponse#ShareFileUploadRangeOptions
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        shareFileAsyncClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(
            Flux.just(defaultData), defaultData.remaining())).subscribe(
                response -> { },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete deleting the file!"));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeWithResponse#ShareFileUploadRangeOptions
    }

    /**
     * Generates a code sample for using
     * {@link ShareFileAsyncClient#upload(Flux, com.azure.storage.common.ParallelTransferOptions)}
     */
    public void upload() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.upload#Flux-ParallelTransferOptions
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        shareFileAsyncClient.upload(Flux.just(defaultData), null).subscribe(
                response -> { },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete deleting the file!"));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.upload#Flux-ParallelTransferOptions
    }

    /**
     * Generates a code sample for using
     * {@link ShareFileAsyncClient#uploadWithResponse(ShareFileUploadOptions)}
     */
    public void uploadBufferedRangeWithResponse() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#ShareFileUploadOptions
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        shareFileAsyncClient.uploadWithResponse(new ShareFileUploadOptions(
            Flux.just(defaultData))).subscribe(
                response -> { },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#ShareFileUploadOptions
    }

    /**
     * Generates a code sample for using
     * {@link ShareFileAsyncClient#uploadWithResponse(Flux, long, Long, ShareRequestConditions)}
     */
    public void uploadDataWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        shareFileAsyncClient.uploadWithResponse(Flux.just(defaultData), defaultData.remaining(), 0L, requestConditions)
            .subscribe(
                response -> { },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete deleting the file!")
            );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadWithResponse#flux-long-long-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#clearRange(long)}
     */
    public void clearRangeAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long
        shareFileAsyncClient.clearRange(1024).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete clearing the range!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#clearRangeWithResponse(long, long)}
     */
    public void clearRangeAsyncMaxOverload() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long
        shareFileAsyncClient.clearRangeWithResponse(1024, 1024).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete clearing the range!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#clearRangeWithResponse(long, long, ShareRequestConditions)}
     */
    public void clearRangeAsyncWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.clearRangeWithResponse(1024, 1024, requestConditions).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete clearing the range!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.clearRange#long-long-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#uploadFromFile(String)}
     */
    public void uploadFileAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string
        shareFileAsyncClient.uploadFromFile("someFilePath").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#uploadFromFile(String, ShareRequestConditions)}
     */
    public void uploadFileAsyncWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.uploadFromFile("someFilePath", requestConditions).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadFromFile#string-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#uploadRangeFromUrl(long, long, long, String)}
     */
    public void uploadFileFromURLAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrl#long-long-long-String
        shareFileAsyncClient.uploadRangeFromUrl(6, 8, 0, "sourceUrl").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Completed upload range from url!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrl#long-long-long-String
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#uploadRangeFromUrlWithResponse(long, long, long, String)}
     */
    public void uploadFileFromURLWithResponseAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String
        shareFileAsyncClient.uploadRangeFromUrlWithResponse(6, 8, 0, "sourceUrl").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Completed upload range from url!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#uploadRangeFromUrlWithResponse(long, long, long, String)}
     */
    public void uploadFileFromURLOptionsBagWithResponseAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#ShareFileUploadRangeFromUrlOptions
        shareFileAsyncClient.uploadRangeFromUrlWithResponse(
            new ShareFileUploadRangeFromUrlOptions(6, "sourceUrl").setDestinationOffset(8))
            .subscribe(
                response -> { },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Completed upload range from url!"));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#ShareFileUploadRangeFromUrlOptions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#uploadRangeFromUrlWithResponse(long, long, long, String, ShareRequestConditions)}
     */
    public void uploadFileFromURLWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.uploadRangeFromUrlWithResponse(6, 8, 0, "sourceUrl", requestConditions).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Completed upload range from url!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-String-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#download()}
     */
    public void downloadDataAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.download
        shareFileAsyncClient.download().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.download
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#downloadWithResponse(ShareFileRange, Boolean)}
     */
    public void downloadWithProperties() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean
        shareFileAsyncClient.downloadWithResponse(new ShareFileRange(1024, 2047L), false)
            .subscribe(response ->
                    System.out.printf("Complete downloading the data with status code %d%n", response.getStatusCode()),
                error -> System.err.println(error.getMessage())
            );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#downloadWithResponse(ShareFileRange, Boolean, ShareRequestConditions)}
     */
    public void downloadWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.downloadWithResponse(new ShareFileRange(1024, 2047L), false, requestConditions)
            .subscribe(response ->
                    System.out.printf("Complete downloading the data with status code %d%n", response.getStatusCode()),
                error -> System.err.println(error.getMessage())
            );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileRange-Boolean-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#downloadWithResponse(ShareFileDownloadOptions)}
     */
    public void downloadWithOptions() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileDownloadOptions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        ShareFileRange range = new ShareFileRange(1024, 2047L);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(3);
        ShareFileDownloadOptions options = new ShareFileDownloadOptions().setRange(range)
            .setRequestConditions(requestConditions)
            .setRangeContentMd5(false)
            .setRetryOptions(retryOptions);
        shareFileAsyncClient.downloadWithResponse(options)
            .subscribe(response ->
                    System.out.printf("Complete downloading the data with status code %d%n", response.getStatusCode()),
                error -> System.err.println(error.getMessage())
            );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.downloadWithResponse#ShareFileDownloadOptions
    }


    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#downloadToFile(String)}
     */
    public void downloadFileAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.downloadToFile#string
        shareFileAsyncClient.downloadToFile("somelocalfilepath").subscribe(
            response -> {
                if (Files.exists(Paths.get("somelocalfilepath"))) {
                    System.out.println("Successfully downloaded the file.");
                }
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the file!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.downloadToFile#string
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#downloadToFileWithResponse(String, ShareFileRange)}
     */
    public void downloadFileAsyncMaxOverload() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange
        shareFileAsyncClient.downloadToFileWithResponse("somelocalfilepath", new ShareFileRange(1024, 2047L))
            .subscribe(
                response -> {
                    if (Files.exists(Paths.get("somelocalfilepath"))) {
                        System.out.println("Successfully downloaded the file with status code "
                            + response.getStatusCode());
                    }
                },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete downloading the file!")
            );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#downloadToFileWithResponse(String, ShareFileRange, ShareRequestConditions)}
     */
    public void downloadFileAsyncWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.downloadToFileWithResponse("somelocalfilepath", new ShareFileRange(1024, 2047L),
            requestConditions)
            .subscribe(
                response -> {
                    if (Files.exists(Paths.get("somelocalfilepath"))) {
                        System.out.println("Successfully downloaded the file with status code "
                            + response.getStatusCode());
                    }
                },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete downloading the file!")
            );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.downloadToFileWithResponse#string-ShareFileRange-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#delete()}
     */
    public void deleteFileAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.delete
        shareFileAsyncClient.delete().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#deleteWithResponse()}
     */
    public void deleteWithResponse() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse
        shareFileAsyncClient.deleteWithResponse().subscribe(
            response -> System.out.println("Complete deleting the file with status code:" + response.getStatusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#deleteWithResponse(ShareRequestConditions)}
     */
    public void deleteWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse#ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.deleteWithResponse(requestConditions).subscribe(
            response -> System.out.println("Complete deleting the file with status code:" + response.getStatusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.deleteWithResponse#ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.getProperties
        shareFileAsyncClient.getProperties()
            .subscribe(properties -> {
                System.out.printf("File latest modified date is %s.", properties.getLastModified());
            });
        // END: com.azure.storage.file.share.ShareFileAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse
        shareFileAsyncClient.getPropertiesWithResponse()
            .subscribe(response -> {
                ShareFileProperties properties = response.getValue();
                System.out.printf("File latest modified date is %s.", properties.getLastModified());
            });
        // END: com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#getPropertiesWithResponse(ShareRequestConditions)}
     */
    public void getPropertiesWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse#ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.getPropertiesWithResponse(requestConditions)
            .subscribe(response -> {
                ShareFileProperties properties = response.getValue();
                System.out.printf("File latest modified date is %s.", properties.getLastModified());
            });
        // END: com.azure.storage.file.share.ShareFileAsyncClient.getPropertiesWithResponse#ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setMetadata(Map)}
     */
    public void setMetadataAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setMetadata#map
        shareFileAsyncClient.setMetadata(Collections.singletonMap("file", "updatedMetadata"))
            .doOnSuccess(response -> System.out.println("Setting the file metadata completed."));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setMetadataWithResponse(Map)}
     */
    public void setMetadataWithResponse() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map
        shareFileAsyncClient.setMetadataWithResponse(Collections.singletonMap("file", "updatedMetadata"))
            .subscribe(response -> System.out.printf("Setting the file metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setMetadataWithResponse(Map, ShareRequestConditions)}
     */
    public void setMetadataWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.setMetadataWithResponse(Collections.singletonMap("file", "updatedMetadata"), requestConditions)
            .subscribe(response -> System.out.printf("Setting the file metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setMetadataWithResponse(Map)} to clear metadata.
     */
    public void clearMetadataAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map.clearMetadata
        shareFileAsyncClient.setMetadataWithResponse(null).subscribe(
            response -> System.out.printf("Setting the file metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setMetadataWithResponse(Map, ShareRequestConditions)} to clear
     * metadata.
     */
    public void clearMetadataAsyncWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions.clearMetadata
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.setMetadataWithResponse(null, requestConditions).subscribe(
            response -> System.out.printf("Setting the file metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setMetadataWithResponse#map-ShareRequestConditions.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setMetadata#map.clearMetadata
        shareFileAsyncClient.setMetadata(null).subscribe(
            response -> System.out.println("Setting the file metadata completed.")
        );
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setProperties(long, ShareFileHttpHeaders, FileSmbProperties, String)}
     */
    public void setFilePropertiesAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        shareFileAsyncClient.setProperties(1024, httpHeaders, smbProperties, filePermission)
            .doOnSuccess(response -> System.out.println("Setting the file properties completed."));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setPropertiesWithResponse(long, ShareFileHttpHeaders, FileSmbProperties, String)}
     */
    public void setHttpHeadersWithResponse() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        shareFileAsyncClient.setPropertiesWithResponse(1024, httpHeaders, smbProperties, filePermission)
            .subscribe(response -> System.out.printf("Setting the file properties completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setPropertiesWithResponse(long, ShareFileHttpHeaders, FileSmbProperties, String, ShareRequestConditions)}
     */
    public void setHttpHeadersWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.setPropertiesWithResponse(1024, httpHeaders, smbProperties, filePermission, requestConditions)
            .subscribe(response -> System.out.printf("Setting the file properties completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setPropertiesWithResponse(long, ShareFileHttpHeaders, FileSmbProperties, String)}
     * to clear httpHeaders.
     */
    public void clearHTTPHeadersAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties
        shareFileAsyncClient.setPropertiesWithResponse(1024, null, null, null)
            .subscribe(response -> System.out.printf("Setting the file httpHeaders completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setPropertiesWithResponse(long, ShareFileHttpHeaders, FileSmbProperties, String, ShareRequestConditions)}
     * to clear httpHeaders.
     */
    public void clearHTTPHeadersAsyncWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions.clearHttpHeaderspreserveSMBProperties
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.setPropertiesWithResponse(1024, null, null, null, requestConditions)
            .subscribe(response -> System.out.printf("Setting the file httpHeaders completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#setProperties(long, ShareFileHttpHeaders, FileSmbProperties, String)}
     * to clear httpHeaders.
     */
    public void clearHTTPHeaders() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties
        shareFileAsyncClient.setProperties(1024, null, null, null)
            .subscribe(response -> System.out.println("Setting the file httpHeaders completed."));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#listRanges()}
     */
    public void listRangesAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.listRanges
        shareFileAsyncClient.listRanges().subscribe(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.getStart(), range.getEnd()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.listRanges
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#listRanges(ShareFileRange)}
     */
    public void listRangesAsyncMaxOverload() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange
        shareFileAsyncClient.listRanges(new ShareFileRange(1024, 2048L))
            .subscribe(result -> System.out.printf("List ranges completed with start: %d, end: %d",
                result.getStart(), result.getEnd()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#listRangesDiff(String)}
     */
    public void listRangesDiffAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiff#String
        final String prevSnapshot = "previoussnapshot";
        shareFileAsyncClient.listRangesDiff(prevSnapshot).subscribe(response -> {
            System.out.println("Valid Share File Ranges are:");
            for (FileRange range : response.getRanges()) {
                System.out.printf("Start: %s, End: %s%n", range.getStart(), range.getEnd());
            }
        });
        // END: com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiff#String
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#listRangesDiffWithResponse(ShareFileListRangesDiffOptions)}
     */
    public void listRangesDiffAsyncOptionalOverload() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiffWithResponse#ShareFileListRangesDiffOptions
        shareFileAsyncClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions("previoussnapshot")
            .setRange(new ShareFileRange(1024, 2048L))).subscribe(response -> {
                System.out.println("Valid Share File Ranges are:");
                for (FileRange range : response.getValue().getRanges()) {
                    System.out.printf("Start: %s, End: %s%n", range.getStart(), range.getEnd());
                }
            });
        // END: com.azure.storage.file.share.ShareFileAsyncClient.listRangesDiffWithResponse#ShareFileListRangesDiffOptions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#listRanges(ShareFileRange, ShareRequestConditions)}
     */
    public void listRangesAsyncWithLease() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        shareFileAsyncClient.listRanges(new ShareFileRange(1024, 2048L), requestConditions)
            .subscribe(result -> System.out.printf("List ranges completed with start: %d, end: %d",
                result.getStart(), result.getEnd()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.listRanges#ShareFileRange-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#listHandles()}
     */
    public void listHandlesAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.listHandles
        shareFileAsyncClient.listHandles()
            .subscribe(result -> System.out.printf("List handles completed with handle id %s", result.getHandleId()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.listHandles
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#listHandles(Integer)}
     */
    public void listHandlesAsyncMaxOverload() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.listHandles#integer
        shareFileAsyncClient.listHandles(10)
            .subscribe(result -> System.out.printf("List handles completed with handle id %s", result.getHandleId()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.listHandles#integer
    }

    /**
     * Code snippet for {@link ShareFileAsyncClient#forceCloseHandle(String)}.
     */
    public void forceCloseHandle() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithConnectionString();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandle#String
        shareFileAsyncClient.listHandles().subscribe(handleItem ->
            shareFileAsyncClient.forceCloseHandle(handleItem.getHandleId()).subscribe(ignored ->
                System.out.printf("Closed handle %s on resource %s%n",
                    handleItem.getHandleId(), handleItem.getPath())));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandle#String
    }

    /**
     * Code snippet for {@link ShareFileAsyncClient#forceCloseHandleWithResponse(String)}.
     */
    public void forceCloseHandleWithResponse() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithConnectionString();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandleWithResponse#String
        shareFileAsyncClient.listHandles().subscribe(handleItem ->
            shareFileAsyncClient.forceCloseHandleWithResponse(handleItem.getHandleId()).subscribe(response ->
                System.out.printf("Closing handle %s on resource %s completed with status code %d%n",
                    handleItem.getHandleId(), handleItem.getPath(), response.getStatusCode())));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.forceCloseHandleWithResponse#String
    }

    /**
     * Code snippet for {@link ShareFileAsyncClient#forceCloseAllHandles()}.
     */
    public void forceCloseAllHandles() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithConnectionString();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.forceCloseAllHandles
        shareFileAsyncClient.forceCloseAllHandles().subscribe(handlesClosedInfo ->
            System.out.printf("Closed %d open handles on the file.%nFailed to close %d open handles on the file%n",
                handlesClosedInfo.getClosedHandles(), handlesClosedInfo.getFailedHandles()));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.forceCloseAllHandles
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#getShareSnapshotId()}
     */
    public void getShareSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareFileAsyncClient shareFileAsyncClient = new ShareFileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .resourcePath("myfiile")
            .snapshot(currentTime.toString())
            .buildFileAsyncClient();

        System.out.printf("Snapshot ID: %s%n", shareFileAsyncClient.getShareSnapshotId());
        // END: com.azure.storage.file.share.ShareFileAsyncClient.getShareSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#getShareName()}
     */
    public void getShareNameAsync() {
        ShareFileAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.getShareName
        String shareName = directoryAsyncClient.getShareName();
        System.out.println("The share name of the directory is " + shareName);
        // END: com.azure.storage.file.share.ShareFileAsyncClient.getShareName
    }

    /**
     * Generates a code sample for using {@link ShareFileAsyncClient#getFilePath()}
     */
    public void getFilePathAsync() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.getFilePath
        String filePath = shareFileAsyncClient.getFilePath();
        System.out.println("The name of the file is " + filePath);
        // END: com.azure.storage.file.share.ShareFileAsyncClient.getFilePath
    }

    /**
     * Code snippet for {@link ShareFileAsyncClient#generateSas(ShareServiceSasSignatureValues)}
     */
    public void generateSas() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        ShareFileSasPermission permission = new ShareFileSasPermission().setReadPermission(true);

        ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        shareFileAsyncClient.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues
    }

    /**
     * Code snippet for {@link ShareFileAsyncClient#generateSas(ShareServiceSasSignatureValues, Context)}
     */
    public void generateSasWithContext() {
        ShareFileAsyncClient shareFileAsyncClient = createAsyncClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues-Context
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        ShareFileSasPermission permission = new ShareFileSasPermission().setReadPermission(true);

        ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        // Client must be authenticated via StorageSharedKeyCredential
        shareFileAsyncClient.generateSas(values, new Context("key", "value"));
        // END: com.azure.storage.file.share.ShareFileAsyncClient.generateSas#ShareServiceSasSignatureValues-Context
    }
}
