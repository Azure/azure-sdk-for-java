// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalFilePurgerTests {

    @TempDir
    File tempFolder;

    @Test
    public void testPurgedExpiredFiles() throws Exception {
        String text = "hello world";
        LocalFileCache cache = new LocalFileCache(tempFolder);
        LocalFileWriter writer = new LocalFileWriter(50, cache, tempFolder, null, false);

        // persist 100 files to disk
        for (int i = 0; i < 100; i++) {
            writer.writeToDisk(
                "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEE;IngestionEndpoint=http://foo.bar/",
                singletonList(ByteBuffer.wrap(text.getBytes(UTF_8))), "original error message");
        }

        List<File> files = FileUtil.listTrnFiles(tempFolder);
        assertThat(files.size()).isEqualTo(100);

        // run purge task every second to delete files that are 1 second old
        LocalFilePurger purger = new LocalFilePurger(tempFolder, 1L, 1L, false);

        Thread.sleep(5000); // wait 5 seconds

        files = FileUtil.listTrnFiles(tempFolder);
        assertThat(files.size()).isEqualTo(0);

        purger.shutdown();
    }
}
