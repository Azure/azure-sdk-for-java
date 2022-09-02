/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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

        // run purge task every second to delete files that are 5 seconds old
        LocalFilePurger purger = new LocalFilePurger(tempFolder, 5L, 1L, false);

        // persist 100 files to disk
        for (int i = 0; i < 100; i++) {
            writer.writeToDisk(
                "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEE;IngestionEndpoint=http://foo.bar/",
                singletonList(ByteBuffer.wrap(text.getBytes(UTF_8))));
        }

        List<File> files = FileUtil.listTrnFiles(tempFolder);
        assertThat(files.size()).isEqualTo(100);

        Thread.sleep(10000); // wait 10 seconds

        files = FileUtil.listTrnFiles(tempFolder);
        assertThat(files.size()).isEqualTo(0);

        purger.shutdown();
    }
}
