// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LocalFileCacheTests {

    private static final Queue<Long> sortedLastModified = new ConcurrentLinkedDeque<>();
    @TempDir
    File tempFolder;

    @BeforeEach
    public void setup() throws Exception {
        List<File> unsortedFiles = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            File tempFile = createTempFile(tempFolder);
            File trnFile = new File(tempFolder, FileUtil.getBaseName(tempFile) + ".trn");
            tempFile.renameTo(trnFile);
            unsortedFiles.add(trnFile);
        }

        unsortedFiles.sort(Comparator.comparing(File::lastModified));
        for (File file : unsortedFiles) {
            sortedLastModified.add(file.lastModified());
        }

        List<File> files = FileUtil.listTrnFiles(tempFolder);
        assertThat(files.size()).isEqualTo(100);
        assertThat(files.size()).isEqualTo(sortedLastModified.size());
    }

    @Test
    public void testSortPersistedFiles() {
        LocalFileCache cache = new LocalFileCache(tempFolder);
        Queue<File> sortedPersistedFile = cache.getPersistedFilesCache();

        assertThat(sortedPersistedFile.size()).isEqualTo(sortedLastModified.size());

        while (sortedPersistedFile.peek() != null && sortedLastModified.peek() != null) {
            File actualFile = sortedPersistedFile.poll();
            Long actualLastModified = actualFile.lastModified();
            Long expectedLastModified = sortedLastModified.poll();
            assertThat(actualLastModified).isEqualTo(expectedLastModified);
        }
    }

    private static File createTempFile(File folder) throws IOException {
        String prefix = System.currentTimeMillis() + "-";
        return File.createTempFile(prefix, null, folder);
    }
}
