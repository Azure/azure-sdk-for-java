// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.implementation.utils.FileUtils;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtilsTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    public void writeToFileAsyncCreatesNewFile() throws IOException {
        Path destinationFile = temporaryDirectory.resolve("new-file.txt");

        StepVerifier
            .create(FileUtils.writeToFileAsync(BinaryData.fromString("new content"), destinationFile.toString(), false))
            .verifyComplete();

        Assertions.assertEquals("new content", new String(Files.readAllBytes(destinationFile), StandardCharsets.UTF_8));
    }

    @Test
    public void writeToFileAsyncFailsWhenFileExistsAndOverwriteDisabled() throws IOException {
        Path destinationFile = Files.createFile(temporaryDirectory.resolve("existing-file.txt"));

        StepVerifier
            .create(
                FileUtils.writeToFileAsync(BinaryData.fromString("updated content"), destinationFile.toString(), false))
            .verifyError(FileAlreadyExistsException.class);
    }

    @Test
    public void writeToFileAsyncOverwritesExistingFileWhenOverwriteEnabled() throws IOException {
        Path destinationFile = Files.createFile(temporaryDirectory.resolve("existing-file.txt"));

        StepVerifier
            .create(
                FileUtils.writeToFileAsync(BinaryData.fromString("updated content"), destinationFile.toString(), true))
            .verifyComplete();

        Assertions.assertEquals("updated content",
            new String(Files.readAllBytes(destinationFile), StandardCharsets.UTF_8));
    }

    @Test
    public void writeToFileCreatesNewFile() throws IOException {
        Path destinationFile = temporaryDirectory.resolve("new-sync-file.txt");

        FileUtils.writeToFile(BinaryData.fromString("new content"), destinationFile.toString(), false);

        Assertions.assertEquals("new content", new String(Files.readAllBytes(destinationFile), StandardCharsets.UTF_8));
    }

    @Test
    public void writeToFileFailsWhenFileExistsAndOverwriteDisabled() throws IOException {
        Path destinationFile = Files.createFile(temporaryDirectory.resolve("existing-sync-file.txt"));

        Assertions.assertThrows(FileAlreadyExistsException.class,
            () -> FileUtils.writeToFile(BinaryData.fromString("updated content"), destinationFile.toString(), false));
    }

    @Test
    public void writeToFileOverwritesExistingFileWhenOverwriteEnabled() throws IOException {
        Path destinationFile = Files.createFile(temporaryDirectory.resolve("existing-sync-file.txt"));

        FileUtils.writeToFile(BinaryData.fromString("updated content"), destinationFile.toString(), true);

        Assertions.assertEquals("updated content",
            new String(Files.readAllBytes(destinationFile), StandardCharsets.UTF_8));
    }
}
