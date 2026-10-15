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

    @Test
    public void computeSha256MatchesKnownVectorForAbc() {
        // SHA-256("abc") standard test vector.
        Assertions.assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            FileUtils.computeSha256(BinaryData.fromString("abc")));
    }

    @Test
    public void computeSha256MatchesKnownVectorForEmptyContent() {
        // SHA-256("") standard test vector.
        Assertions.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            FileUtils.computeSha256(BinaryData.fromBytes(new byte[0])));
    }

    @Test
    public void computeSha256ReturnsLowercaseHexDigestOf64Characters() {
        String hash = FileUtils.computeSha256(BinaryData.fromString("some code content"));

        Assertions.assertTrue(hash.matches("[0-9a-f]{64}"),
            "Expected a 64-character lowercase hex digest but was: " + hash);
    }

    @Test
    public void computeSha256IsConsistentAcrossBinaryDataSources() throws IOException {
        String text = "code-zip-content";
        Path file = Files.write(temporaryDirectory.resolve("code.zip"), text.getBytes(StandardCharsets.UTF_8));

        String fromBytes = FileUtils.computeSha256(BinaryData.fromBytes(text.getBytes(StandardCharsets.UTF_8)));
        String fromString = FileUtils.computeSha256(BinaryData.fromString(text));
        String fromFile = FileUtils.computeSha256(BinaryData.fromFile(file));

        Assertions.assertEquals(fromBytes, fromString);
        Assertions.assertEquals(fromBytes, fromFile);
    }

    @Test
    public void computeSha256HashesFileBackedBinaryContent() throws IOException {
        // Mimics the real code-zip path: bytes with all values (incl. > 0x7f) read from disk via BinaryData.fromFile.
        byte[] binaryContent = new byte[256];
        for (int i = 0; i < binaryContent.length; i++) {
            binaryContent[i] = (byte) i;
        }
        Path file = Files.write(temporaryDirectory.resolve("binary.zip"), binaryContent);

        Assertions.assertEquals(FileUtils.computeSha256(BinaryData.fromBytes(binaryContent)),
            FileUtils.computeSha256(BinaryData.fromFile(file)));
    }

    @Test
    public void computeSha256IsRepeatableForFileBackedContent() throws IOException {
        // The client hashes the content and then re-reads it for upload, so hashing must be replayable/stable.
        byte[] bytes = "repeatable".getBytes(StandardCharsets.UTF_8);
        BinaryData content = BinaryData.fromFile(Files.write(temporaryDirectory.resolve("repeat.zip"), bytes));

        Assertions.assertEquals(FileUtils.computeSha256(content), FileUtils.computeSha256(content));
    }

    @Test
    public void computeSha256DiffersForDifferentContent() {
        Assertions.assertNotEquals(FileUtils.computeSha256(BinaryData.fromString("content-a")),
            FileUtils.computeSha256(BinaryData.fromString("content-b")));
    }

    @Test
    public void writeToFileFailsWhenDestinationIsADirectory() throws IOException {
        Path directoryDestination = Files.createDirectory(temporaryDirectory.resolve("dir-destination"));

        // Passing a directory where a file path is expected must fail rather than silently succeed, regardless of the
        // overwrite flag.
        Assertions.assertThrows(IOException.class,
            () -> FileUtils.writeToFile(BinaryData.fromString("content"), directoryDestination.toString(), false));
        Assertions.assertThrows(IOException.class,
            () -> FileUtils.writeToFile(BinaryData.fromString("content"), directoryDestination.toString(), true));
    }

    @Test
    public void writeToFileAsyncFailsWhenDestinationIsADirectory() throws IOException {
        Path directoryDestination = Files.createDirectory(temporaryDirectory.resolve("dir-destination-async"));

        StepVerifier
            .create(FileUtils.writeToFileAsync(BinaryData.fromString("content"), directoryDestination.toString(), true))
            .verifyError(IOException.class);
    }
}
