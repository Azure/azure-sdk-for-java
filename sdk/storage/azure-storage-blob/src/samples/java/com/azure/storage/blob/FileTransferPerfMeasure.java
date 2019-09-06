// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.commons.lang3.time.StopWatch;
import reactor.netty.resources.ConnectionProvider;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * This class shows how to upload the file as fast as possible in parallel using the optimized upload API.
 */
public class FileTransferPerfMeasure {
    private static final String LARGE_TEST_FOLDER = "test-large-files/";
    private static final int MB = 1024 * 1024;
    private static final long GB = 1024L * MB;

    /**
     * Entry point into the file transfer examples for Storage blobs.
     * @param args Unused. Arguments to the program.
     * @throws IOException If an I/O error occurs
     * @throws NoSuchAlgorithmException If {@code MD5} isn't supported
     * @throws RuntimeException If the uploaded or downloaded file wasn't found
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        /*
         * From the Azure portal, get your Storage account's name and account key.
         */
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        SharedKeyCredential credential = new SharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endPoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         * Now you can use the storageClient to perform various container and blob operations.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder()
            .httpClient(new NettyAsyncHttpClientBuilder().build())
            .retryOptions(new RequestRetryOptions(null, null, 600, null, null, null))
            .endpoint(endPoint).credential(credential).buildClient();

        /*
         * Create a client that references a to-be-created container in your Azure Storage account. This returns a
         * ContainerClient uses the same endpoint, credential and pipeline from storageClient.
         * Note that container names require lowercase.
         */
        ContainerClient containerClient = storageClient.getContainerClient("myjavacontainerparallelupload" + System.currentTimeMillis());

        /*
         * Create a container in Storage blob account.
         */
        containerClient.create();

        /*
         * Create a BlockBlobClient object that wraps a blob's endpoint and a default pipeline, the blockBlobClient give us access to upload the file.
         */
        String filename = "BigFile.bin";
        BlockBlobClient blobClient = containerClient.getBlockBlobClient(filename);


        /*
         * Generate random things to uploadFile, which makes the file with size of 100MB.
         */
        long fileSize = 2 * GB;
        File largeFile = createTempRandomFile(filename, fileSize);
        File downloadFile = createTempEmptyFile("downloaded.bin");

        /*
         * Initialize stopwatch.
         */
        StopWatch stopWatch = new StopWatch();
        long initialMemory = Runtime.getRuntime().totalMemory();
        int initialThreads = Thread.activeCount();
        MetricMaxRecorder memoryRecorder = new MetricMaxRecorder() {
            @Override
            public long getMetric() {
                return Runtime.getRuntime().totalMemory();
            }
        };

        MetricMaxRecorder threadRecorder = new MetricMaxRecorder() {
            @Override
            public long getMetric() {
                return Thread.activeCount();
            }
        };

        memoryRecorder.run();
        threadRecorder.run();

        /*
         * Upload the large file to storage blob.
         */
        stopWatch.start();
        blobClient.uploadFromFile(largeFile.getPath());
        stopWatch.stop();

        long uploadSeconds = stopWatch.getTime(TimeUnit.SECONDS);
        int uploadThreads = (int) threadRecorder.getMax();
        long uploadMemory = memoryRecorder.getMax();
        threadRecorder.reset();
        memoryRecorder.reset();

        /*
         * Download the large file from storage blob to the local downloadFile path.
         */
        stopWatch.reset();
        stopWatch.start();
        blobClient.downloadToFile(downloadFile.getPath(), null, 100 * MB, null, null, false, null, null);
        stopWatch.stop();

        long downloadSeconds = stopWatch.getTime(TimeUnit.SECONDS);
        int downloadThreads = (int) threadRecorder.getMax();
        long downloadMemory = memoryRecorder.getMax();
        threadRecorder.stop();
        memoryRecorder.stop();

        System.out.println("| Scenario | Speed\t | Memory\t | Threads |");
        System.out.println(String.format(
            "| Initial  | N/A\t | %dMB\t | %d\t |", 0, initialThreads));
        System.out.println(String.format(
            "| Upload   | %dMB/s\t | %dMB\t | %d\t |", (fileSize/MB)/uploadSeconds, (uploadMemory - initialMemory)/MB, uploadThreads));
        System.out.println(String.format(
            "| Download | %dMB/s\t | %dMB\t | %d\t |", (fileSize/MB)/downloadSeconds, (downloadMemory - initialMemory)/MB, downloadThreads));

        /*
         * Clean up the local files and storage container.
         */
        containerClient.delete();
        Files.deleteIfExists(largeFile.toPath());
        Files.deleteIfExists(downloadFile.toPath());
    }

    private static File createTempEmptyFile(String fileName) throws IOException {
        String pathName = Paths.get(LARGE_TEST_FOLDER).toString();

        File dirPath = new File(pathName);

        if (dirPath.exists() || dirPath.mkdir()) {
            File f = new File(pathName + "/" + fileName);
            if (f.exists() || f.createNewFile()) {
                return f;
            } else {
                throw new RuntimeException("Failed to create the large file.");
            }
        } else {
            throw new RuntimeException("Failed to create the large file dir.");
        }
    }

    private static File createTempRandomFile(String fileName, long fileSize) throws IOException {
        File file = createTempEmptyFile(fileName);
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            Random random = new Random();
            int chunkSize = 4 * MB;
            byte[] chunk = new byte[chunkSize];
            random.nextBytes(chunk);

            long numChunks = (long) Math.ceil(((double) fileSize) / chunkSize);
            for (long i = 0; i != numChunks; i++) {
                ByteBuffer buffer = ByteBuffer.wrap(chunk);
                channel.write(buffer, i * chunkSize);
            }
        }
        return file;
    }

    private static abstract class MetricMaxRecorder implements Runnable {
        private long metric;
        private boolean isAlive = true;

        public abstract long getMetric();

        public int interval() {
            return 1000;
        }

        public long getMax() {
            return metric;
        }

        public final void reset() {
            metric = -1;
        }

        public final void stop() {
            isAlive = false;
        }

        @Override
        public void run() {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!isAlive) {
                        cancel();
                    } else {
                        long current = getMetric();
                        if (current > metric) {
                            metric = current;
                        }
                        try {
                            Thread.sleep(interval());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 0, interval());
        }
    }
}
