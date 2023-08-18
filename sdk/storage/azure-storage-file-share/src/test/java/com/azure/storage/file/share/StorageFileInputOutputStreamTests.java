// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class StorageFileInputOutputStreamTests extends FileShareTestBase {
    private ShareFileClient fileClient;
    private int length;

    @BeforeEach
    public void setup() {
        String shareName = generateShareName();
        String filePath = generatePathName();
        ShareClient shareClient = shareBuilderHelper(shareName).buildClient();
        shareClient.create();
        fileClient = shareClient.getFileClient(filePath);
    }

    @EnabledIf("com.azure.storage.file.share.FileShareTestBase#isLiveMode")
    @Test
    public void uploadDownload() throws IOException {
        length = 30 * Constants.MB;
        fileClient.create(length);
        byte[] randomBytes = FileShareTestHelper.getRandomBuffer(length);

        StorageFileOutputStream outStream = fileClient.getFileOutputStream();
        outStream.write(randomBytes);
        outStream.close();

        StorageFileInputStream inputStream = fileClient.openInputStream();
        int b;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        byte[] randomBytes2 = outputStream.toByteArray();
        assertArrayEquals(randomBytes2, randomBytes);
    }


    @EnabledIf("com.azure.storage.file.share.FileShareTestBase#isLiveMode")
    @Test
    public void streamWithOffset() throws IOException {
        length = 7 * Constants.MB;
        fileClient.create(length);
        byte[] randomBytes = FileShareTestHelper.getRandomBuffer(9 * Constants.MB);

        StorageFileOutputStream outStream = fileClient.getFileOutputStream();
        outStream.write(randomBytes, 2 * Constants.MB, length);
        outStream.close();

        StorageFileInputStream inputStream = fileClient.openInputStream();
        byte[] b = new byte[length];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            if (inputStream.read(b) != -1) {
                outputStream.write(b, 0, b.length);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        byte[] randomBytes2 = outputStream.toByteArray();
        assertArrayEquals(randomBytes2, Arrays.copyOfRange(randomBytes, 2 * Constants.MB, 9 * Constants.MB));
    }
}
