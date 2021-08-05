// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileQueryDelimitedSerialization;
import com.azure.storage.file.datalake.models.FileQueryError;
import com.azure.storage.file.datalake.models.FileQueryJsonSerialization;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import com.azure.storage.file.datalake.options.FileQueryOptions;
import com.azure.storage.file.datalake.models.FileQueryProgress;
import com.azure.storage.file.datalake.models.FileQuerySerialization;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.options.FileScheduleDeletionOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Code snippets for {@link DataLakeFileClient}
 */
@SuppressWarnings({"unused"})
public class DataLakeFileClientJavaDocSamples {
    private String fileName = "fileName";
    private DataLakeFileClient client = JavaDocCodeSnippetsHelpers.getFileClient(fileName);
    private String leaseId = "leaseId";
    private Duration timeout = Duration.ofSeconds(30);
    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";
    private String destinationPath = "destinationPath";
    private String fileSystemName = "fileSystemName";
    private String filePath = "filePath";
    private InputStream data = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
    private long offset = 0L;
    private long length = 4L;
    private long position = 4L;
    private String file = "file";

    /**
     * Code snippets for {@link DataLakeFileClient#delete()} and
     * {@link DataLakeFileClient#deleteWithResponse(DataLakeRequestConditions, Duration, Context)}
     */
    public void deleteCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.delete
        client.delete();
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.delete

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.deleteWithResponse#DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);

        client.deleteWithResponse(requestConditions, timeout, new Context(key1, value1));
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.deleteWithResponse#DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeFileClient#rename(String, String)} and
     * {@link DataLakeFileClient#renameWithResponse(String, String, DataLakeRequestConditions, DataLakeRequestConditions, Duration, Context)}
     */
    public void renameCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.rename#String-String
        DataLakeFileClient renamedClient = client.rename(fileSystemName, destinationPath);
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.rename#String-String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions();

        DataLakeFileClient newRenamedClient = client.renameWithResponse(fileSystemName, destinationPath,
            sourceRequestConditions, destinationRequestConditions, timeout, new Context(key1, value1)).getValue();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeFileClient#read(OutputStream)} and
     * {@link DataLakeFileClient#readWithResponse(OutputStream, FileRange, DownloadRetryOptions, DataLakeRequestConditions, boolean, Duration, Context)}
     */
    public void readCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.read#OutputStream
        client.read(new ByteArrayOutputStream());
        System.out.println("Download completed.");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.read#OutputStream

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.readWithResponse#OutputStream-FileRange-DownloadRetryOptions-DataLakeRequestConditions-boolean-Duration-Context
        FileRange range = new FileRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        System.out.printf("Download completed with status %d%n",
            client.readWithResponse(new ByteArrayOutputStream(), range, options, null, false,
                timeout, new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.file.datalake.DataLakeFileClient.readWithResponse#OutputStream-FileRange-DownloadRetryOptions-DataLakeRequestConditions-boolean-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeFileClient#readToFile(String)} and
     * {@link DataLakeFileClient#readToFileWithResponse(String, FileRange, ParallelTransferOptions, DownloadRetryOptions, DataLakeRequestConditions,
     * boolean, Set, Duration, Context)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.readToFile#String
        client.readToFile(file);
        System.out.println("Completed download to file");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.readToFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.readToFile#String-boolean
        boolean overwrite = false; // Default value
        client.readToFile(file, overwrite);
        System.out.println("Completed download to file");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.readToFile#String-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.readToFileWithResponse#String-FileRange-ParallelTransferOptions-DownloadRetryOptions-DataLakeRequestConditions-boolean-Set-Duration-Context
        FileRange fileRange = new FileRange(1024, 2048L);
        DownloadRetryOptions downloadRetryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);
        Set<OpenOption> openOptions = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE, StandardOpenOption.READ)); // Default options

        client.readToFileWithResponse(file, fileRange, new ParallelTransferOptions().setBlockSizeLong(4L * Constants.MB),
            downloadRetryOptions, null, false, openOptions, timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.readToFileWithResponse#String-FileRange-ParallelTransferOptions-DownloadRetryOptions-DataLakeRequestConditions-boolean-Set-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeFileClient#upload(InputStream, long)},
     * {@link DataLakeFileClient#upload(InputStream, long, boolean)}, and
     * {@link DataLakeFileClient#uploadWithResponse(FileParallelUploadOptions, Duration, Context)}
     */
    public void uploadCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.upload#InputStream-long
        try {
            client.upload(data, length);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.file.datalake.DataLakeFileClient.upload#InputStream-long

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.upload#InputStream-long-boolean
        try {
            boolean overwrite = false;
            client.upload(data, length, overwrite);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.file.datalake.DataLakeFileClient.upload#InputStream-long-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.uploadWithResponse#FileParallelUploadOptions-Duration-Context
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

        try {
            client.uploadWithResponse(new FileParallelUploadOptions(data, length)
                .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers)
                .setMetadata(metadata).setRequestConditions(requestConditions)
                .setPermissions("permissions").setUmask("umask"), timeout, new Context("key", "value"));
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.file.datalake.DataLakeFileClient.uploadWithResponse#FileParallelUploadOptions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeFileClient#uploadFromFile(String)},
     * {@link DataLakeFileClient#uploadFromFile(String, boolean)} and
     * {@link DataLakeFileClient#uploadFromFile(String, ParallelTransferOptions, PathHttpHeaders, Map, DataLakeRequestConditions, Duration)}
     */
    public void uploadFromFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.uploadFromFile#String
        try {
            client.uploadFromFile(filePath);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.file.datalake.DataLakeFileClient.uploadFromFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.uploadFromFile#String-boolean
        try {
            boolean overwrite = false;
            client.uploadFromFile(filePath, overwrite);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.file.datalake.DataLakeFileClient.uploadFromFile#String-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.uploadFromFile#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions-Duration
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

        try {
            client.uploadFromFile(filePath, parallelTransferOptions, headers, metadata, requestConditions, timeout);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.file.datalake.DataLakeFileClient.uploadFromFile#String-ParallelTransferOptions-PathHttpHeaders-Map-DataLakeRequestConditions-Duration
    }

    /**
     * Code snippets for {@link DataLakeFileClient#append(InputStream, long, long)} and
     * {@link DataLakeFileClient#appendWithResponse(InputStream, long, long, byte[], String, Duration, Context)}
     */
    public void appendCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.append#InputStream-long-long
        client.append(data, offset, length);
        System.out.println("Append data completed");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.append#InputStream-long-long

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.appendWithResponse#InputStream-long-long-byte-String-Duration-Context
        FileRange range = new FileRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);
        byte[] contentMd5 = new byte[0]; // Replace with valid md5

        Response<Void> response = client.appendWithResponse(data, offset, length, contentMd5, leaseId, timeout,
            new Context(key1, value1));
        System.out.printf("Append data completed with status %d%n", response.getStatusCode());
        // END: com.azure.storage.file.datalake.DataLakeFileClient.appendWithResponse#InputStream-long-long-byte-String-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeFileClient#flush(long)}  and
     * {@link DataLakeFileClient#flushWithResponse(long, boolean, boolean, PathHttpHeaders, DataLakeRequestConditions, Duration, Context)}
     */
    public void flushCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.flush#long
        client.flush(position);
        System.out.println("Flush data completed");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.flush#long

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.flush#long-boolean
        boolean overwrite = true;
        client.flush(position, overwrite);
        System.out.println("Flush data completed");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.flush#long-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.flushWithResponse#long-boolean-boolean-PathHttpHeaders-DataLakeRequestConditions-Duration-Context
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

        Response<PathInfo> response = client.flushWithResponse(position, retainUncommittedData, close, httpHeaders,
            requestConditions, timeout, new Context(key1, value1));
        System.out.printf("Flush data completed with status %d%n", response.getStatusCode());
        // END: com.azure.storage.file.datalake.DataLakeFileClient.flushWithResponse#long-boolean-boolean-PathHttpHeaders-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link DataLakeFileClient#openQueryInputStream(String)}
     */
    public void openQueryInputStream() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.openQueryInputStream#String
        String expression = "SELECT * from BlobStorage";
        InputStream inputStream = client.openQueryInputStream(expression);
        // Now you can read from the input stream like you would normally.
        // END: com.azure.storage.file.datalake.DataLakeFileClient.openQueryInputStream#String
    }

    /**
     * Code snippet for {@link DataLakeFileClient#openQueryInputStreamWithResponse(FileQueryOptions)}
     */
    public void openQueryInputStream2() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.openQueryInputStream#FileQueryOptions
        String expression = "SELECT * from BlobStorage";
        FileQuerySerialization input = new FileQueryDelimitedSerialization()
            .setColumnSeparator(',')
            .setEscapeChar('\n')
            .setRecordSeparator('\n')
            .setHeadersPresent(true)
            .setFieldQuote('"');
        FileQuerySerialization output = new FileQueryJsonSerialization()
            .setRecordSeparator('\n');
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId("leaseId");
        Consumer<FileQueryError> errorConsumer = System.out::println;
        Consumer<FileQueryProgress> progressConsumer = progress -> System.out.println("total file bytes read: "
            + progress.getBytesScanned());
        FileQueryOptions queryOptions = new FileQueryOptions(expression)
            .setInputSerialization(input)
            .setOutputSerialization(output)
            .setRequestConditions(requestConditions)
            .setErrorConsumer(errorConsumer)
            .setProgressConsumer(progressConsumer);

        InputStream inputStream = client.openQueryInputStreamWithResponse(queryOptions).getValue();
        // Now you can read from the input stream like you would normally.
        // END: com.azure.storage.file.datalake.DataLakeFileClient.openQueryInputStream#FileQueryOptions
    }

    /**
     * Code snippet for {@link DataLakeFileClient#query(OutputStream, String)}
     */
    public void query() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.query#OutputStream-String
        ByteArrayOutputStream queryData = new ByteArrayOutputStream();
        String expression = "SELECT * from BlobStorage";
        client.query(queryData, expression);
        System.out.println("Query completed.");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.query#OutputStream-String
    }

    /**
     * Code snippet for {@link DataLakeFileClient#queryWithResponse(FileQueryOptions, Duration, Context)}
     */
    public void queryWithResponse() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.queryWithResponse#FileQueryOptions-Duration-Context
        ByteArrayOutputStream queryData = new ByteArrayOutputStream();
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
        FileQueryOptions queryOptions = new FileQueryOptions(expression, queryData)
            .setInputSerialization(input)
            .setOutputSerialization(output)
            .setRequestConditions(requestConditions)
            .setErrorConsumer(errorConsumer)
            .setProgressConsumer(progressConsumer);
        System.out.printf("Query completed with status %d%n",
            client.queryWithResponse(queryOptions, timeout, new Context(key1, value1))
                .getStatusCode());
        // END: com.azure.storage.file.datalake.DataLakeFileClient.queryWithResponse#FileQueryOptions-Duration-Context
    }

    /**
     * Code snippet for {@link DataLakeFileClient#scheduleDeletion(FileScheduleDeletionOptions)}
     */
    public void scheduleDeletion() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.scheduleDeletion#FileScheduleDeletionOptions
        FileScheduleDeletionOptions options = new FileScheduleDeletionOptions(OffsetDateTime.now().plusDays(1));
        client.scheduleDeletion(options);
        System.out.println("File deletion has been scheduled");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.scheduleDeletion#FileScheduleDeletionOptions
    }


    /**
     * Code snippet for {@link DataLakeFileClient#scheduleDeletionWithResponse(FileScheduleDeletionOptions, Duration, Context)}
     */
    public void scheduleDeletionWithResponse() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileClient.scheduleDeletionWithResponse#FileScheduleDeletionOptions-Duration-Context
        FileScheduleDeletionOptions options = new FileScheduleDeletionOptions(OffsetDateTime.now().plusDays(1));
        Context context = new Context("key", "value");

        client.scheduleDeletionWithResponse(options, timeout, context);
        System.out.println("File deletion has been scheduled");
        // END: com.azure.storage.file.datalake.DataLakeFileClient.scheduleDeletionWithResponse#FileScheduleDeletionOptions-Duration-Context
    }

}

