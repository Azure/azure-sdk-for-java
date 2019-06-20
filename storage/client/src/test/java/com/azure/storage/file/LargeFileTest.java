package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.test.InterceptorManager;
import com.azure.storage.StorageTestBase;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.Assert.fail;

public class LargeFileTest extends StorageTestBase {
    private static final String LARGE_TEST_FOLDER = "test-large-files/";
    private static FileClient client;
    private static String fileName;
    private static String filePath;

    @Before
    public void setup() {
        fileName = generateName("largefile10g");
        URL folderUrl = InterceptorManager.class.getClassLoader().getResource(".");
        filePath = new File(folderUrl.getPath() + LARGE_TEST_FOLDER + fileName).getPath();
        client = setupClient((connectionString, endpoint) -> FileClient.builder()
                                                                 .connectionString(connectionString)
                                                                 .shareName("storagefiletests")
                                                                 .filePath(LARGE_TEST_FOLDER + fileName)
                                                                 .endpoint(endpoint)
                                                                 .httpClient(HttpClient.createDefault().wiretap(true))
                                                                 .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                                                                 .build());

    }

    @Test
    public void uploadLargeFile() throws Exception {
        Long fileSize = 10 * 1024 * 1024 * 1024L;
        File largeTmpFile = new File(filePath);
        RandomAccessFile raf = new RandomAccessFile(largeTmpFile, "rw");
        raf.setLength(fileSize);
//        if (!createSparseFile(filePath, fileSize)) {
//            fail("Failed to create sparse file!");
//        }
        client.create(fileSize);
        if (largeTmpFile.exists()) {
            client.uploadFromFile(filePath);
            System.out.println("Uploaded success!");
            client.delete();
            largeTmpFile.delete();
        } else {
            fail("Did not find the upload file.");
        }
    }
//
//    @Test
//    public void downloadLargeBlockBlob() throws Exception {
//        uploadLargeFile();
//        OffsetDateTime start = OffsetDateTime.now();
//        File downloaded = new File(filePath);
//        downloaded.createNewFile();
//        client.downloadToFile(filePath);
//        System.out.println("Download " + downloaded.length() + " bytes took " + Duration.between(start, OffsetDateTime.now()).getSeconds() + " seconds");
//        Path path = Paths.get(filePath);
//        if (Files.exists(path)) {
//            Files.delete(path);
//        } else {
//            fail("Did not find the download file.");
//        }
//    }

    //  private boolean createSparseFile(String filePath, Long fileSize) {
//        boolean success = true;
//        String command = "dd if=/dev/zero of=%s bs=1 count=1 seek=%s";
//        String formmatedCommand = String.format(command, filePath, fileSize);
//        String s;
//        Process p;
//        try {
//            p = Runtime.getRuntime().exec(formmatedCommand);
//            p.waitFor();
//            p.destroy();
//        } catch (IOException | InterruptedException e) {
//            fail(e.getLocalizedMessage());
//        }
//        return success;
    //   }
}
