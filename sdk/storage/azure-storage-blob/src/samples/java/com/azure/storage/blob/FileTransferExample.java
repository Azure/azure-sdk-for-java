// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.common.StorageSharedKeyCredential;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * This class shows how to upload the file as fast as possible in parallel using the optimized upload API.
 */
public class FileTransferExample {
    private static final String LARGE_TEST_FOLDER = "test-large-files/";

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
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endPoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         * Now you can use the storageClient to perform various container and blob operations.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endPoint).credential(credential).buildClient();


        /*
         * This example shows several common operations just to get you started.
         */


        /*
         * Create a client that references a to-be-created container in your Azure Storage account. This returns a
         * ContainerClient uses the same endpoint, credential and pipeline from storageClient.
         * Note that container names require lowercase.
         */
        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient("myjavacontainerparallelupload" + System.currentTimeMillis());

        /*
         * Create a container in Storage blob account.
         */
        blobContainerClient.create();

        /*
         * Create a BlockBlobClient object that wraps a blob's endpoint and a default pipeline, the blockBlobClient give us access to upload the file.
         */
        String filename = "BigFile.bin";
        BlobClient blobClient = blobContainerClient.getBlobClient(filename);

        /*
         * Create the empty uploadFile and downloadFile.
         */
        File largeFile = createTempEmptyFile(filename);

        File downloadFile = createTempEmptyFile("downloadFile.bin");

        /*
         * Generate random things to uploadFile, which makes the file with size of 100MB.
         */
        long fileSize = 100 * 1024 * 1024L;
        createTempFileWithFileSize(largeFile, fileSize);

        /*
         * Upload the large file to storage blob.
         */
        blobClient.uploadFromFile(largeFile.getPath());

        /*
         * Download the large file from storage blob to the local downloadFile path.
         */
        blobClient.downloadToFile(downloadFile.getPath());

        /*
         * Check the files are same after the round-trip.
         */
        if (Files.exists(downloadFile.toPath()) && Files.exists(largeFile.toPath())) {
            checkTwoFilesAreTheSame(largeFile, downloadFile);
            System.out.println("The file we upload is the same as the one we download.");
        } else {
            throw new RuntimeException("Did not find the upload or download file.");
        }

        /*
         * Clean up the local files and storage container.
         */
        blobContainerClient.delete();
        Files.deleteIfExists(largeFile.toPath());
        Files.deleteIfExists(downloadFile.toPath());
    }

    private static File createTempEmptyFile(String fileName) throws IOException {
        String pathName = "./folderPath/" + LARGE_TEST_FOLDER;

        File dirPath = new File(pathName);

        if (dirPath.exists() || dirPath.mkdir()) {
            File f = new File(pathName + fileName);
            if (f.exists() || f.createNewFile()) {
                return f;
            } else {
                throw new RuntimeException("Failed to create the large file.");
            }
        } else {
            throw new RuntimeException("Failed to create the large file dir.");
        }
    }

    private static void createTempFileWithFileSize(File f, long size) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        raf.setLength(size);
        raf.close();
    }

    private static void checkTwoFilesAreTheSame(File f1, File f2) throws IOException, NoSuchAlgorithmException {
        String checksumUpload = getFileChecksum(f1);
        String checksumDownload = getFileChecksum(f2);
        if (!checksumUpload.equals(checksumDownload)) {
            throw new RuntimeException("The file upload does not match the file download.");
        }
    }

    private static String getFileChecksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        try (FileInputStream fis = new FileInputStream(file); FileChannel ch = fis.getChannel()) {
            final ByteBuffer buf = ByteBuffer.allocateDirect(8192);
            int b = ch.read(buf);
            while (b != -1 && b != 0) {
                buf.flip();
                final byte[] bytes = new byte[b];
                buf.get(bytes);
                md.update(bytes, 0, b);
                buf.clear();
                b = ch.read(buf);
            }

            return new String(md.digest(), StandardCharsets.UTF_8);
        }
    }


}
