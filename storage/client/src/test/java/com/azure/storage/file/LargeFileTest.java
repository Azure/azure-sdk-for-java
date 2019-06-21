package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.test.InterceptorManager;
import com.azure.storage.StorageTestBase;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.fail;

public class LargeFileTest extends StorageTestBase {
    private static final String LARGE_TEST_FOLDER = "test-large-files/";
    private static FileClient client;
    private static String fileName;
    private static File largeFile;

    @Before
    public void setup() {
        fileName = generateName("largefile");
        URL folderUrl = InterceptorManager.class.getClassLoader().getResource(".");
        File dirPath = new File(folderUrl.getPath() + LARGE_TEST_FOLDER);
        if (dirPath.exists() || dirPath.mkdir()) {
            largeFile = new File(folderUrl.getPath() + LARGE_TEST_FOLDER + fileName);
        } else {
            System.out.println("Failed to create the large file dir.");
        }

        client = setupClient((connectionString, endpoint) -> FileClient.builder()
                                                                 //.connectionString(connectionString)
                                                                 .shareName("storagefiletests")
                                                                 .filePath(LARGE_TEST_FOLDER + fileName)
                                                                 .endpoint(endpoint)
                                                                 .httpClient(HttpClient.createDefault().wiretap(true))
                                                                 .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                                                                 .build());

    }

    @Test
    public void uploadAndDownloadLargeFile() throws Exception {
        Long fileSize = 10 * 1024 * 1024 * 1024L;
        RandomAccessFile raf = new RandomAccessFile(largeFile, "rw");
        raf.setLength(fileSize);
        client.create(fileSize);
        if (largeFile.exists()) {
            client.uploadFromFile(largeFile.getPath());
            System.out.println("Uploaded success!");
        } else {
            fail("Did not find the upload file.");
        }
        OffsetDateTime start = OffsetDateTime.now();
        File downloadFile = new File(LARGE_TEST_FOLDER + generateName("download"));
        downloadFile.createNewFile();
        client.downloadToFile(downloadFile.getPath());
        System.out.println("Download " + downloadFile.length() + " bytes took " + Duration.between(start, OffsetDateTime.now()).getSeconds() + " seconds");
        if (Files.exists(downloadFile.toPath())) {
            String checksumUpload = getFileChecksum(largeFile);
            String checksumDownload = getFileChecksum(downloadFile);
            Assert.assertEquals(checksumUpload, checksumDownload);
        } else {
            fail("Did not find the download file.");
        }
    }

    private static String getFileChecksum(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Files.readAllBytes(Paths.get(file.getPath())));
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }
}
