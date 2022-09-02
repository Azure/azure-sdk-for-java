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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

class FileUtil {

    private FileUtil() {
    }

    static List<File> listTrnFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".trn"));
        return files == null ? Collections.emptyList() : asList(files);
    }

    static String getBaseName(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        return index == -1 ? name : name.substring(0, index);
    }

    static void moveFile(File srcFile, File destFile) throws IOException {
        if (!srcFile.renameTo(destFile)) {
            throw new IOException(
                "Unable to rename file '"
                    + srcFile.getAbsolutePath()
                    + "' to '"
                    + destFile.getAbsolutePath()
                    + "'");
        }
    }

    // delete a file and then retry 3 times when it fails.
    static boolean deleteFileWithRetries(File file) {
        if (!file.delete()) {
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                if (file.delete()) {
                    break;
                }
            }
        }

        return true;
    }
}
