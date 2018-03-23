/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.storage.samples;

import com.microsoft.azure.storage.blob.BlobRange;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.LoggingOptions;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.azure.storage.blob.TelemetryOptions;
import com.microsoft.rest.v2.RestException;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpPipelineLogger;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BasicSample {
    static HttpPipeline getPipeline(String accountName, String accountKey) throws UnsupportedEncodingException, InvalidKeyException {
        HttpPipelineLogger logger = new HttpPipelineLogger() {
            @Override
            public HttpPipelineLogLevel minimumLogLevel() {
                return HttpPipelineLogLevel.INFO;
            }

            @Override
            public void log(HttpPipelineLogLevel logLevel, String s, Object... objects) {
                if (logLevel == HttpPipelineLogLevel.INFO) {
                    Logger.getGlobal().info(String.format(s, objects));
                } else if (logLevel == HttpPipelineLogLevel.WARNING) {
                    Logger.getGlobal().warning(String.format(s, objects));
                } else if (logLevel == HttpPipelineLogLevel.ERROR) {
                    Logger.getGlobal().severe(String.format(s, objects));
                }
            }
        };
        LoggingOptions loggingOptions = LoggingOptions.DEFAULT;

        SharedKeyCredentials creds = new SharedKeyCredentials(accountName, accountKey);
        TelemetryOptions telemetryOptions = TelemetryOptions.DEFAULT;
        PipelineOptions pop = new PipelineOptions();
        pop.telemetryOptions = telemetryOptions;
        pop.client = HttpClient.createDefault();
        pop.logger = logger;
        pop.loggingOptions = loggingOptions;
        return StorageURL.createPipeline(creds, pop);
    }

    public static void main(String[] args) throws Exception {
        // This sample depends on some knowledge of RxJava.
        // A general primer on Rx can be found at http://reactivex.io/intro.html.

        String accountName = System.getenv("ACCOUNT_NAME");
        String accountKey = System.getenv("ACCOUNT_KEY");
        HttpPipeline pipeline = getPipeline(accountName, accountKey);

        // Objects representing the Azure Storage resources we're sending requests to.
        final ServiceURL serviceURL = new ServiceURL(
                new URL("http://" + accountName + ".blob.core.windows.net"), pipeline);
        final ContainerURL containerURL = serviceURL.createContainerURL("javasdktest");
        final BlockBlobURL blobURL = containerURL.createBlockBlobURL("testBlob");

        // Some data to put into Azure Storage
        final byte[] data = { 0, 1, 2, 3, 4 };

        // Convert the data to the common interface used for streaming transfers.
        final Flowable<ByteBuffer> asyncStream = Flowable.just(ByteBuffer.wrap(data));

        // Comment the above stream and uncomment this to upload a file instead.
        // This shows how to upload a file.

        //AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("myfile"));
        //Flowable<ByteBuffer> asyncStream = FlowableUtil.readFile(AsynchronousFileChannel.open(Paths.get("myfile")));

        Disposable disposable = containerURL.create(null, null)
            .toCompletable()
            .onErrorResumeNext(throwable -> {
                // This method gets called if an error occurred when creating the container.
                // Now we can examine the error and decide if it's recoverable.

                // A RestException is thrown when the HTTP response has an error status such as 404.
                if (throwable instanceof RestException) {
                    HttpResponse response = ((RestException) throwable).response();
                    if (response.statusCode() == 409) {
                        // Status code 409 means the container already exists, so we recover from the error and resume the workflow.
                        return Completable.complete();
                    }
                }

                // If the error wasn't due to an HTTP 409, the error is considered unrecoverable.
                // By propagating the exception we received, the workflow completes without performing the putBlob or getBlob.
                return Completable.error(throwable);
            }) // blobURL.putBlobAsync will be performed unless the container create fails with an unrecoverable error.
            .andThen(blobURL.putBlob(asyncStream, data.length, null, null, null))
            .flatMap(putResponse ->
                // This method is called after the blob is uploaded successfully.
                // Now let's download the blob.
                blobURL.getBlob(new BlobRange(0L, data.length), null, false)
            ).flatMapCompletable(getResponse -> {
                // This method is called after getBlobAsync response headers have come back from the service.
                // We now need to download the blob's contents.

                // Output file path for downloaded blob.
                final Path path = Paths.get("myFilePath");
                final AsynchronousFileChannel outFile = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                return FlowableUtil.writeFile(getResponse.body(), outFile)
                // Adding a .timeout() operator here causes the file download to cancel if it hasn't completed within 1 second.
                    .timeout(1, TimeUnit.SECONDS)
                    .doOnTerminate(outFile::close)
                    .doOnError(err -> Files.delete(path));
            }).subscribe(() -> System.out.println("Finished blob download."));

        // If you want to cancel the operation, you can call .dispose() on the disposable returned by the workflow.
//         disposable.dispose();

        // Wait for the operation to complete.
        System.in.read();
    }
}