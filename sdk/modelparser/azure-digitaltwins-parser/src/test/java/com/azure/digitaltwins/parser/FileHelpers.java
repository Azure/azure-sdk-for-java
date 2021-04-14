// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelpers {
    public static String getTestResourcesDirectoryPath(String subDirectoryName) {
        Path resourceDirectory = Paths.get("src", "test", "resources", subDirectoryName);
        return resourceDirectory.toFile().getAbsolutePath() + "/";
    }

    public static String getTestResourceFilePath(String subDirectoryName, String fileName) {
        Path resourceDirectory = Paths.get("src", "test", "resources", subDirectoryName);
        return resourceDirectory.toFile().getAbsolutePath() + "/" + fileName;
    }

    public static String getFileContentsByPath(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public static String getFileContentsByFileName(String subDirectoryName, String fileName) throws IOException {
        return getFileContentsByPath(getTestResourceFilePath(subDirectoryName, fileName));
    }

    public static void deleteFile(String subDirectoryName, String fileName) {
        File file = new File(FileHelpers.getTestResourceFilePath(subDirectoryName, fileName));
        file.delete();
    }

    public static void deleteJavaFilesInSubDirectory(String subDirectoryName) {
        File generatedCodeDirectory = new File(FileHelpers.getTestResourcesDirectoryPath(subDirectoryName));
        File[] generatedDirectoryFiles = generatedCodeDirectory.listFiles();

        for (File file : generatedDirectoryFiles) {
            if (file.getName().endsWith(".java")) {
                file.delete();
            }
        }
    }
}
