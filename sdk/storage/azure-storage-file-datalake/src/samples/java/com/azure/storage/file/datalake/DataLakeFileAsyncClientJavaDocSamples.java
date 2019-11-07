// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.util.Context;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Code snippets for {@link DataLakeFileAsyncClient}
 */
@SuppressWarnings({"unused"})
public class DataLakeFileAsyncClientJavaDocSamples {
    private String fileName = "fileName";
    private DataLakeFileAsyncClient client = JavaDocCodeSnippetsHelpers.getFileAsyncClient(fileName);
    private String leaseId = "leaseId";
    private String destinationPath = "destinationPath";
    private Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8)));
    private long length = 4L;
    private long position = 4L;
    private long offset = 0L;

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
     * Code snippets for {@link DataLakeFileAsyncClient#rename(String)} and
     * {@link DataLakeFileAsyncClient#renameWithResponse(String, DataLakeRequestConditions, DataLakeRequestConditions)}
     */
    public void renameCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.rename#String
        DataLakeFileAsyncClient renamedClient = client.rename(destinationPath).block();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.rename#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileAsyncClient.renameWithResponse#String-DataLakeRequestConditions-DataLakeRequestConditions
        DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions();

        DataLakeFileAsyncClient newRenamedClient = client.renameWithResponse(destinationPath, sourceRequestConditions,
            destinationRequestConditions).block().getValue();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeFileAsyncClient.renameWithResponse#String-DataLakeRequestConditions-DataLakeRequestConditions
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

}
