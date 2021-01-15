// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.util.Context;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileQueryDelimitedSerialization;
import com.azure.storage.file.datalake.models.FileQueryError;
import com.azure.storage.file.datalake.models.FileQueryJsonSerialization;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import com.azure.storage.file.datalake.options.FileQueryOptions;
import com.azure.storage.file.datalake.models.FileQueryProgress;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.options.FileScheduleDeletionOptions;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Code snippets for {@link DataLakeFileAsyncClient}
 */
@SuppressWarnings({"unused"})
public class DataLakeFileAsyncClientJavaDocSamples {
    private String fileName = "fileName";
    private DataLakeFileAsyncClient client = JavaDocCodeSnippetsHelpers.getFileAsyncClient(fileName);
    private String leaseId = "leaseId";
    private String destinationPath = "destinationPath";
    private String fileSystemName = "fileSystemName";
    private String filePath = "filePath";
    private Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8)));
    private long length = 4L;
    private long position = 4L;
    private long offset = 0L;
    private String file = "file";

    /**
     * Code snippets for {@link DataLakeFileAsyncClient#delete()} and
     * {@link DataLakeFileAsyncClient#deleteWithResponse(DataLakeRequestConditions)}
     */
    public void deleteCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.delete
        client.delete().subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.delete

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.deleteWithResponse#DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);

        client.deleteWithResponse(requestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.deleteWithResponse#DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeFileAsyncClient#rename(String, String)} and
     * {@link DataLakeFileAsyncClient#renameWithResponse(String, String, DataLakeRequestConditions, DataLakeRequestConditions)}
     */
    public void renameCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.rename#String-String
        DataLakeFileAsyncClient renamedClient = client.rename(fileSystemName, destinationPath).block();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.rename#String-String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions
        DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions();

        DataLakeFileAsyncClient newRenamedClient = client.renameWithResponse(fileSystemName, destinationPath,
            sourceRequestConditions, destinationRequestConditions).block().getValue();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeFileAsyncClient#read()} and
     * {@link DataLakeFileAsyncClient#readWithResponse(FileRange, DownloadRetryOptions, DataLakeRequestConditions, boolean)}
     * @throws UncheckedIOException if the read fails.
     */
    public void readCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.read
        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        client.read().subscribe(piece -> {
            try {
                downloadData.write(piece.array());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.read

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.readWithResponse#FileRange-DownloadRetryOptions-DataLakeRequestConditions-boolean
        FileRange range = new FileRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        client.readWithResponse(range, options, null, false).subscribe(response -> {
            ByteArrayOutputStream readData = new ByteArrayOutputStream();
            response.getValue().subscribe(piece -> {
                try {
                    readData.write(piece.array());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        });
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.readWithResponse#FileRange-DownloadRetryOptions-DataLakeRequestConditions-boolean
    }

    /**
     * Code snippets for {@link DataLakeFileAsyncClient#readToFile(String)} and {@link DataLakeFileAsyncClient#readToFileWithResponse(String,
     * FileRange, ParallelTransferOptions, DownloadRetryOptions, DataLakeRequestConditions, boolean, Set)}
     */
    public void downloadToFileCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String
        client.readToFile(file).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String-boolean
        boolean overwrite = false; // Default value
        client.readToFile(file, overwrite).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFile#String-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFileWithResponse#String-FileRange-ParallelTransferOptions-DownloadRetryOptions-DataLakeRequestConditions-boolean-Set
        FileRange fileRange = new FileRange(1024, 2048L);
        DownloadRetryOptions downloadRetryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);
        Set<OpenOption> openOptions = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE, StandardOpenOption.READ)); // Default options

        client.readToFileWithResponse(file, fileRange, null, downloadRetryOptions, null, false, openOptions)
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.readToFileWithResponse#String-FileRange-ParallelTransferOptions-DownloadRetryOptions-DataLakeRequestConditions-boolean-Set
    }

    /**
     * Code snippets for {@link DataLakeFileAsyncClient#upload(Flux, ParallelTransferOptions)},
     * {@link DataLakeFileAsyncClient#upload(Flux, ParallelTransferOptions, boolean)}, and
     * {@link DataLakeFileAsyncClient#uploadWithResponse(Flux, ParallelTransferOptions, PathHttpHeaders, Map, DataLakeRequestConditions)}
     */
    public void uploadCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions
        client.uploadFromFile(filePath)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions-boolean
        boolean overwrite = false; // Default behavior
        client.uploadFromFile(filePath, overwrite)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.upload#Flux-ParallelTransferOptions-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions
        PathHttpHeaders headers = new PathHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Long blockSize = 100L * 1024L * 1024L; // 100 MB;
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize);

        client.uploadWithResponse(data, parallelTransferOptions, headers, metadata, requestConditions)
            .subscribe(response -> System.out.println("Uploaded file %n"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions.ProgressReporter
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadataMap = Collections.singletonMap("metadata", "value");
        DataLakeRequestConditions conditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        ParallelTransferOptions pto = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setProgressReceiver(bytesTransferred -> System.out.printf("Upload progress: %s bytes sent", bytesTransferred));

        client.uploadWithResponse(data, pto, httpHeaders, metadataMap, conditions)
            .subscribe(response -> System.out.println("Uploaded file %n"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions.ProgressReporter
    }

    /**
     * Code snippets for {@link DataLakeFileAsyncClient#uploadWithResponse(FileParallelUploadOptions)}
     */
    public void uploadCodeSnippets2() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions
        PathHttpHeaders headers = new PathHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Long blockSize = 100L * 1024L * 1024L; // 100 MB;
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize);

        client.uploadWithResponse(new FileParallelUploadOptions(data)
            .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers)
            .setMetadata(metadata).setRequestConditions(requestConditions)
            .setPermissions("permissions").setUmask("umask"))
            .subscribe(response -> System.out.println("Uploaded file %n"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions.ProgressReporter
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadataMap = Collections.singletonMap("metadata", "value");
        DataLakeRequestConditions conditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        ParallelTransferOptions pto = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setProgressReceiver(bytesTransferred -> System.out.printf("Upload progress: %s bytes sent", bytesTransferred));

        client.uploadWithResponse(new FileParallelUploadOptions(data)
            .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers)
            .setMetadata(metadata).setRequestConditions(requestConditions)
            .setPermissions("permissions").setUmask("umask"))
            .subscribe(response -> System.out.println("Uploaded file %n"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadWithResponse#FileParallelUploadOptions.ProgressReporter
    }

    /**
     * Code snippets for {@link DataLakeFileAsyncClient#uploadFromFile(String)},
     * {@link DataLakeFileAsyncClient#uploadFromFile(String, boolean)} and
     * {@link DataLakeFileAsyncClient#uploadFromFile(String, ParallelTransferOptions, PathHttpHeaders, Map, DataLakeRequestConditions)}
     */
    public void uploadFromFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String
        client.uploadFromFile(filePath)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-boolean
        boolean overwrite = false; // Default behavior
        client.uploadFromFile(filePath, overwrite)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions
        PathHttpHeaders headers = new PathHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Long blockSize = 100L * 1024L * 1024L; // 100 MB;
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize);

        client.uploadFromFile(filePath, parallelTransferOptions, headers, metadata, requestConditions)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.uploadFromFile#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeFileAsyncClient#append(Flux, long, long)} and
     * {@link DataLakeFileAsyncClient#appendWithResponse(Flux, long, long, byte[], String, Context)}
     */
    public void appendCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.append#Flux-long-long
        client.append(data, offset, length)
            .subscribe(
                response -> System.out.println("Append data completed"),
                error -> System.out.printf("Error when calling append data: %s", error));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.append#Flux-long-long

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#Flux-long-long-byte-String
        FileRange range = new FileRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);
        byte[] contentMd5 = new byte[0]; // Replace with valid md5

        client.appendWithResponse(data, offset, length, contentMd5, leaseId).subscribe(response ->
            System.out.printf("Append data completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.appendWithResponse#Flux-long-long-byte-String
    }

    /**
     * Code snippets for {@link DataLakeFileAsyncClient#flush(long)}  and
     * {@link DataLakeFileAsyncClient#flushWithResponse(long, boolean, boolean, PathHttpHeaders, DataLakeRequestConditions)}
     */
    public void flushCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long
        client.flush(position).subscribe(response ->
            System.out.println("Flush data completed"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long-boolean
        boolean overwrite = true;
        client.flush(position, overwrite).subscribe(response ->
            System.out.println("Flush data completed"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.flush#long-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.flushWithResponse#long-boolean-boolean-PathHttpHeaders-DataLakeRequestConditions
        FileRange range = new FileRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);
        byte[] contentMd5 = new byte[0]; // Replace with valid md5
        boolean retainUncommittedData = false;
        boolean close = false;
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);

        client.flushWithResponse(position, retainUncommittedData, close, httpHeaders,
            requestConditions).subscribe(response ->
            System.out.printf("Flush data completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.flushWithResponse#long-boolean-boolean-PathHttpHeaders-DataLakeRequestConditions
    }

    /**
     * Code snippet for {@link DataLakeFileAsyncClient#query(String)}
     * @throws UncheckedIOException for IOExceptions.
     */
    public void query() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.query#String
        ByteArrayOutputStream queryData = new ByteArrayOutputStream();
        String expression = "SELECT * from BlobStorage";
        client.query(expression).subscribe(piece -> {
            try {
                queryData.write(piece.array());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.query#String
    }

    /**
     * Code snippet for {@link DataLakeFileAsyncClient#queryWithResponse(FileQueryOptions)}
     * @throws UncheckedIOException for IOExceptions.
     */
    public void queryWithResponse() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.queryWithResponse#FileQueryOptions
        String expression = "SELECT * from BlobStorage";
        FileQueryJsonSerialization input = new FileQueryJsonSerialization()
            .setRecordSeparator('\n');
        FileQueryDelimitedSerialization output = new FileQueryDelimitedSerialization()
            .setEscapeChar('\0')
            .setColumnSeparator(',')
            .setRecordSeparator('\n')
            .setFieldQuote('\'')
            .setHeadersPresent(true);
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        Consumer<FileQueryError> errorConsumer = System.out::println;
        Consumer<FileQueryProgress> progressConsumer = progress -> System.out.println("total file bytes read: "
            + progress.getBytesScanned());
        FileQueryOptions queryOptions = new FileQueryOptions(expression)
            .setInputSerialization(input)
            .setOutputSerialization(output)
            .setRequestConditions(requestConditions)
            .setErrorConsumer(errorConsumer)
            .setProgressConsumer(progressConsumer);

        client.queryWithResponse(queryOptions)
            .subscribe(response -> {
                ByteArrayOutputStream queryData = new ByteArrayOutputStream();
                response.getValue().subscribe(piece -> {
                    try {
                        queryData.write(piece.array());
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
            });
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.queryWithResponse#FileQueryOptions
    }

    /**
     * Code snippet for {@link DataLakeFileAsyncClient#scheduleDeletion(FileScheduleDeletionOptions)}
     */
    public void scheduleDeletion() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletion#FileScheduleDeletionOptions
        FileScheduleDeletionOptions options = new FileScheduleDeletionOptions(OffsetDateTime.now().plusDays(1));

        client.scheduleDeletion(options)
            .subscribe(r -> System.out.println("File deletion has been scheduled"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletion#FileScheduleDeletionOptions
    }

    /**
     * Code snippet for {@link DataLakeFileAsyncClient#scheduleDeletionWithResponse(FileScheduleDeletionOptions)}
     */
    public void scheduleDeletionWithResponse() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletionWithResponse#FileScheduleDeletionOptions
        FileScheduleDeletionOptions options = new FileScheduleDeletionOptions(OffsetDateTime.now().plusDays(1));

        client.scheduleDeletionWithResponse(options)
            .subscribe(r -> System.out.println("File deletion has been scheduled"));
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.scheduleDeletionWithResponse#FileScheduleDeletionOptions
    }

}
