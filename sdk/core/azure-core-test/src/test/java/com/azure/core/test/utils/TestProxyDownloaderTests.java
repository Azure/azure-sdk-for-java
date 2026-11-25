// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestProxyDownloaderTests {
    private static final String ASSET_NAME = "test-proxy-standalone-linux-x64.tar.gz";
    private static final String ABC_SHA256 = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @Test
    public void cacheIsPerUser() {
        Path home = Paths.get(System.getProperty("user.home")).toAbsolutePath().normalize();
        assertTrue(TestProxyDownloader.getProxyDirectory().startsWith(home));
        assertNotEquals(Paths.get(System.getProperty("java.io.tmpdir"), "test-proxy"),
            TestProxyDownloader.getProxyDirectory());
    }

    @Test
    public void readsMatchingPublishedDigest() throws IOException {
        String metadata = "{\"assets\":[{\"name\":\"other\",\"digest\":\"sha256:invalid\"},"
            + "{\"uploader\":{\"name\":\"ignored\"},\"digest\":\"sha256:" + ABC_SHA256 + "\",\"name\":\"" + ASSET_NAME
            + "\"}]}";
        try (JsonReader reader = JsonProviders.createReader(metadata)) {
            assertEquals(ABC_SHA256, TestProxyDownloader.readExpectedDigest(reader, ASSET_NAME));
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{\"assets\":[]}",
            "{\"assets\":[{\"name\":\"other\"}]}",
            "{\"assets\":[{\"name\":\"test-proxy-standalone-linux-x64.tar.gz\",\"digest\":null}]}",
            "{\"assets\":[{\"name\":\"test-proxy-standalone-linux-x64.tar.gz\",\"digest\":\"sha256:123\"}]}",
            "{\"assets\":[{\"name\":\"test-proxy-standalone-linux-x64.tar.gz\",\"digest\":\"md5:123\"}]}" })
    public void rejectsMissingOrInvalidPublishedDigest(String metadata) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(metadata)) {
            assertThrows(IOException.class, () -> TestProxyDownloader.readExpectedDigest(reader, ASSET_NAME));
        }
    }

    @Test
    public void rejectsDuplicatePublishedAssets() throws IOException {
        String asset = "{\"name\":\"" + ASSET_NAME + "\",\"digest\":\"sha256:" + ABC_SHA256 + "\"}";
        try (JsonReader reader = JsonProviders.createReader("{\"assets\":[" + asset + "," + asset + "]}")) {
            assertThrows(IOException.class, () -> TestProxyDownloader.readExpectedDigest(reader, ASSET_NAME));
        }
    }

    @Test
    public void verifiesContentsRatherThanVersionMarker(@TempDir Path directory) throws IOException {
        Path installation = Files.createDirectory(directory.resolve("bin"));
        Path executable = installation.resolve(TestProxyUtils.getProxyProcessName());
        Path version = directory.resolve("version.txt");
        Files.write(executable, "abc".getBytes(StandardCharsets.UTF_8));
        Files.write(version, Arrays.asList("version", ABC_SHA256), StandardCharsets.UTF_8);
        assertTrue(TestProxyDownloader.checkDownloadedVersion(installation, version, "version"));
        assertFalse(TestProxyDownloader.checkDownloadedVersion(installation, version, "other-version"));

        Files.write(executable, "tampered".getBytes(StandardCharsets.UTF_8));
        assertThrows(IOException.class,
            () -> TestProxyDownloader.checkDownloadedVersion(installation, version, "version"));
        assertThrows(IOException.class, () -> TestProxyDownloader.verifyDigest(executable, ABC_SHA256));
    }

    @Test
    public void rejectsIncompleteCache(@TempDir Path directory) throws IOException {
        Path installation = Files.createDirectory(directory.resolve("bin"));
        Path version = directory.resolve("version.txt");
        assertFalse(TestProxyDownloader.checkDownloadedVersion(installation, version, "version"));
        Files.write(version, Collections.singletonList("version"), StandardCharsets.UTF_8);
        assertThrows(IOException.class,
            () -> TestProxyDownloader.checkDownloadedVersion(installation, version, "version"));
        Files.write(version, Arrays.asList("version", ABC_SHA256), StandardCharsets.UTF_8);
        assertThrows(IOException.class,
            () -> TestProxyDownloader.checkDownloadedVersion(installation, version, "version"));
    }

    @Test
    public void privateDirectoryCanBeReused(@TempDir Path directory) throws IOException {
        Path cache = directory.resolve("cache");
        TestProxyDownloader.ensurePrivateDirectory(cache);
        assertDoesNotThrow(() -> TestProxyDownloader.ensurePrivateDirectory(cache));
        if (Files.getFileAttributeView(cache, PosixFileAttributeView.class) != null) {
            assertEquals(PosixFilePermissions.fromString("rwx------"), Files.getPosixFilePermissions(cache));
        }
    }

    @Test
    public void rejectsNonPrivatePosixCache(@TempDir Path directory) throws IOException {
        assumeTrue(Files.getFileAttributeView(directory, PosixFileAttributeView.class) != null);
        Path cache = Files.createDirectory(directory.resolve("cache"));
        Files.setPosixFilePermissions(cache, PosixFilePermissions.fromString("rwxrwxrwx"));
        assertThrows(IOException.class, () -> TestProxyDownloader.ensurePrivateDirectory(cache));
    }

    @Test
    public void rejectsPosixHomeWritableByOtherUsers(@TempDir Path home) throws IOException {
        assumeTrue(Files.getFileAttributeView(home, PosixFileAttributeView.class) != null);
        Files.setPosixFilePermissions(home, PosixFilePermissions.fromString("rwxr-x---"));
        assertDoesNotThrow(() -> TestProxyDownloader.ensurePrivateHome(home));
        Files.setPosixFilePermissions(home, PosixFilePermissions.fromString("rwxrwx---"));
        assertThrows(IOException.class, () -> TestProxyDownloader.ensurePrivateHome(home));
    }

    @Test
    public void nonPosixHomeOwnerIsNotChecked() {
        Path home = Paths.get(System.getProperty("user.home"));
        assumeTrue(Files.getFileAttributeView(home, PosixFileAttributeView.class) == null);
        // Windows user profiles are commonly owned by SYSTEM or Administrators rather than the current user.
        assertDoesNotThrow(() -> TestProxyDownloader.ensurePrivateHome(home));
    }

    @Test
    public void rejectsSymbolicLinks(@TempDir Path directory) throws IOException {
        assumeTrue(Files.getFileAttributeView(directory, PosixFileAttributeView.class) != null);
        Path target = Files.createDirectory(directory.resolve("target"));
        Path link = Files.createSymbolicLink(directory.resolve("cache"), target);
        assertThrows(IOException.class, () -> TestProxyDownloader.ensurePrivateDirectory(link));
        assertThrows(IOException.class, () -> TestProxyDownloader.verifyDigest(link, ABC_SHA256));
    }

    @Test
    public void archiveCannotEscapePrivateInstallation(@TempDir Path directory) {
        assertThrows(IllegalArgumentException.class,
            () -> TestProxyDownloader.getArchiveOutputFile(directory, "../executable"));
        assertThrows(IllegalArgumentException.class, () -> TestProxyDownloader.getArchiveOutputFile(directory,
            directory.getParent().resolve("outside").toString()));
        assertEquals(directory.resolve("executable"),
            TestProxyDownloader.getArchiveOutputFile(directory, "./executable"));
        assertEquals(directory, TestProxyDownloader.getArchiveOutputFile(directory, "./"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void extractsRootDirectoryAndExecutable(boolean tarGz, @TempDir Path directory) throws Exception {
        Path archive = directory.resolve(tarGz ? "proxy.tar.gz" : "proxy.zip");
        String executable = TestProxyUtils.getProxyProcessName();
        byte[] content = "abc".getBytes(StandardCharsets.UTF_8);
        if (tarGz) {
            try (TarOutputStream output = new TarOutputStream(new GZIPOutputStream(Files.newOutputStream(archive)))) {
                output.putNextEntry(new TarEntry("./"));
                output.closeEntry();
                TarEntry entry = new TarEntry("./" + executable);
                entry.setSize(content.length);
                output.putNextEntry(entry);
                output.write(content);
                output.closeEntry();
            }
        } else {
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive))) {
                output.putNextEntry(new ZipEntry("./"));
                output.closeEntry();
                output.putNextEntry(new ZipEntry("./" + executable));
                output.write(content);
                output.closeEntry();
            }
        }
        Path installation = directory.resolve("bin");
        TestProxyDownloader.extractVerifiedArchive(archive, installation, tarGz);
        assertDoesNotThrow(() -> TestProxyDownloader.verifyDigest(installation.resolve(executable), ABC_SHA256));
        assertTrue(Files.isExecutable(installation.resolve(executable)));
        assertTrue(Files.exists(archive));
    }
}
