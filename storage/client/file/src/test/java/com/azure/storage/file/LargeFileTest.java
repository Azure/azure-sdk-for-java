// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class LargeFileTest extends TestBase {
    private final ClientLogger largeFileLogger = new ClientLogger(LargeFileTest.class);
    private final String azureStorageConnectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_CONNECTION_STRING");
    private static final String LARGE_TEST_FOLDER = "test-large-files/";
    private static FileClient largeFileClient;
    private static String fileName;
    private static File largeFile;
    private ShareClient shareClient;
    private File dirPath;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setup() {
        assumeTrue("The test is only for live mode.", FileTestHelpers.getTestMode() == TestMode.RECORD);
        fileName = testResourceNamer.randomName("largefile", 16);
        URL folderUrl = LargeFileTest.class.getClassLoader().getResource(".");
        dirPath = new File(folderUrl.getPath() + LARGE_TEST_FOLDER);
        if (dirPath.exists() || dirPath.mkdir()) {
            largeFile = new File(folderUrl.getPath() + LARGE_TEST_FOLDER + fileName);
        } else {
            largeFileLogger.warning("Failed to create the large file dir.");
        }
        String shareName = testResourceNamer.randomName("largefileshare", 32);
        shareClient = new ShareClientBuilder().connectionString(azureStorageConnectionString).shareName(shareName)
                                    .buildClient();
        shareClient.create();
        shareClient.createDirectory("largefiledir");
        largeFileClient = shareClient.getDirectoryClient("largefiledir").getFileClient(fileName);
    }

    @Ignore
    @Test
    public void uploadAndDownloadLargeFile() throws Exception {
        Long fileSize = 5 * 1024 * 1024L;
        RandomAccessFile raf = new RandomAccessFile(largeFile, "rw");
        raf.setLength(fileSize);
        largeFileClient.create(fileSize);
        if (largeFile.exists()) {
            largeFileClient.uploadFromFile(largeFile.getPath());
            largeFileLogger.warning("Uploaded success!");
        } else {
            fail("Did not find the upload file.");
        }
        OffsetDateTime start = OffsetDateTime.now();
        File downloadFile = new File(dirPath + "/" + testResourceNamer.randomName("download", 16));
        downloadFile.createNewFile();
        largeFileClient.downloadToFile(downloadFile.getPath());
        System.out.println("Download " + downloadFile.length() + " bytes took " + Duration.between(start, OffsetDateTime.now()).getSeconds() + " seconds");
        if (Files.exists(downloadFile.toPath()) && Files.exists(largeFile.toPath())) {
            String checksumUpload = getFileChecksum(largeFile);
            String checksumDownload = getFileChecksum(downloadFile);
            Assert.assertEquals(checksumUpload, checksumDownload);
        } else {
            fail("Did not find the download file.");
        }
    }

    @After
    public void cleanUp() {
        assumeTrue("The test is only for live mode.", FileTestHelpers.getTestMode() == TestMode.RECORD);
        shareClient.delete();
        FileTestHelpers.sleepInRecordMode(Duration.ofSeconds(45));
    }

    private String getFileChecksum(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Files.readAllBytes(file.toPath()));
        int numRead;
        InputStream fis =  new FileInputStream(file.getPath());
        byte[] buffer = new byte[1024];
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                md.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        byte[] digestByte = md.digest();
        String result = "";
        for (int i = 0; i < digestByte.length; i++) {
            result += Integer.toString((digestByte[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     * Gets the name of the current test being run.
     * <p>
     * NOTE: This could not be implemented in the base class using {@link TestName} because it always returns {@code
     * null}. See https://stackoverflow.com/a/16113631/4220757.
     *
     * @return The name of the current test.
     */
    @Override
    protected String testName() {
        return testName.getMethodName();
    }
}
