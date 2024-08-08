// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

class FileUtil {

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
                "Unable to rename file '" + srcFile.getAbsolutePath() + "' to '" + destFile.getAbsolutePath() + "'");
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

    private FileUtil() {
    }
}
