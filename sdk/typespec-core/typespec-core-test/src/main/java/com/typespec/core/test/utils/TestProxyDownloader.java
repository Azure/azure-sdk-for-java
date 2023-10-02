// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils;

import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.logging.LogLevel;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

/**
 * Class for managing downloads of the test proxy
 */
public final class TestProxyDownloader {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyDownloader.class);
    private static final Path PROXY_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "test-proxy");

    private static String testProxyTag;
    private TestProxyDownloader() { }

    /**
     * Reports the directory the test proxy was installed to.
     * @return A {@link Path} with the test proxy location.
     */
    public static Path getProxyDirectory() {
        return PROXY_PATH;
    }

    /**
     * Requests that the test proxy be downloaded and unpacked. If it is already present this is a no-op.
     * @param testClassPath the test class path
     */
    public static void installTestProxy(Path testClassPath) {
        testProxyTag = TestProxyUtils.getTestProxyVersion(testClassPath);
        if (!checkDownloadedVersion()) {
            PlatformInfo platformInfo = new PlatformInfo();
            downloadProxy(platformInfo);
            extractTestProxy(platformInfo);
        }
    }

    private static void extractTestProxy(PlatformInfo platformInfo) {
        Path zipFile = getZipFileLocation(platformInfo.getExtension());
        if (Files.exists(PROXY_PATH)) {
            try {
                Files.walk(PROXY_PATH)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Could not delete old test proxy zip file %s", zipFile.toString()), e);
            }
        }

        try {
            if (platformInfo.extension.equals("tar.gz")) {
                try (InputStream file = Files.newInputStream(zipFile);
                     InputStream buffer = new BufferedInputStream(file);
                     GZIPInputStream gzipInputStream = new GZIPInputStream(buffer);
                     ArchiveInputStream archive = new TarArchiveInputStream(gzipInputStream)) {
                    decompress(archive);
                }
            } else {
                try (InputStream file = Files.newInputStream(zipFile);
                     InputStream buffer = new BufferedInputStream(file);
                     ArchiveInputStream archive = new ZipArchiveInputStream(buffer)) {
                    decompress(archive);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void decompress(ArchiveInputStream archive) {
        try {
            ArchiveEntry entry = archive.getNextEntry();

            while (entry != null) {

                File outputFile = getOutputFile(entry);
                if (entry.isDirectory()) {
                    if (!outputFile.isDirectory() && !outputFile.mkdirs()) {
                        throw new RuntimeException("Could not create all required directories");
                    }
                } else {
                    File parent = outputFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new RuntimeException("Could not create all required directories");
                    }
                    try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
                        IOUtils.copy(archive, outputStream);
                        if (outputFile.getName().equals(TestProxyUtils.getProxyProcessName())) {
                            outputFile.setExecutable(true, false);
                        }
                    }
                }
                entry = archive.getNextEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getOutputFile(ArchiveEntry entry) {
        return new File(PROXY_PATH.toFile(), entry.getName());
    }


    private static void downloadProxy(PlatformInfo platformInfo) {
        LOGGER.log(LogLevel.INFORMATIONAL, () -> "Downloading test proxy. This may take a few moments.");

        try {
            URL url = UrlBuilder.parse(getProxyDownloadUrl(platformInfo)).toUrl();
            Files.copy(url.openStream(),
                getZipFileLocation(platformInfo.getExtension()),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not save test proxy download", e);
        }

        updateDownloadedFileVersion();
    }

    private static Path getZipFileLocation(String extension) {
        return Paths.get(System.getProperty("java.io.tmpdir"), String.format("testproxy.%s", extension));
    }

    private static void updateDownloadedFileVersion() {
        Path filePath = getFileVersionPath();
        try {
            Files.write(filePath, Arrays.asList(testProxyTag));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write version data to file", e);
        }
    }

    private static boolean checkDownloadedVersion() {
        Path filePath = getFileVersionPath();
        if (!Files.exists(filePath)) {
            return false;
        }
        String fileVersion;
        try {
            fileVersion = Files.readAllLines(filePath).get(0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read version data from file", e);
        }
        return fileVersion.equals(testProxyTag);
    }

    private static Path getFileVersionPath() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "test-proxy-version.txt");
    }

    private static String getProxyDownloadUrl(PlatformInfo platformInfo) {
        return String.format("https://github.com/Azure/azure-sdk-tools/releases/download/Azure.Sdk.Tools.TestProxy_%s/test-proxy-standalone-%s-%s.%s",
            testProxyTag,
            platformInfo.getPlatform(),
            platformInfo.getArchitecture(),
            platformInfo.getExtension());
    }

    private static class PlatformInfo {
        private final String platform;
        private final String extension;
        private final String architecture;


        PlatformInfo() {
            String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            if (osName.contains("windows")) {
                platform = "win";
                extension = "zip";
            } else if (osName.contains("linux")) {
                platform = "linux";
                extension = "tar.gz";
            } else if (osName.contains("mac os x")) {
                platform = "osx";
                extension = "zip";
            } else {
                throw new RuntimeException("unexpected osName " + osName);
            }
            String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
            // intel Macs are x86_64.
            if (arch.contains("amd64") || arch.contains("x86_64")) {
                architecture = "x64";
            } else if (arch.contains("arm64")) {
                architecture = "arm64";
            } else {
                throw new RuntimeException("Unsupported platform " + arch);
            }
        }

        public String getPlatform() {
            return platform;
        }

        public String getArchitecture() {
            return architecture;
        }

        public String getExtension() {
            return extension;
        }
    }
}
