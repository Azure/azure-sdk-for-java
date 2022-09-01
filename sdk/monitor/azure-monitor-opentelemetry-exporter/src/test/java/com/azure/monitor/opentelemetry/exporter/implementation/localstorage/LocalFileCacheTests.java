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
  @TempDir File tempFolder;

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
