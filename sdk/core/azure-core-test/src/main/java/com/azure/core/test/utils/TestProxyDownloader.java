// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class for managing downloads of the test proxy
 */
public final class TestProxyDownloader {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyDownloader.class);
    private static final Path PROXY_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "test-proxy");

    private static String testProxyTag;

    private TestProxyDownloader() {
    }

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
        try {
            if (Files.exists(PROXY_PATH)) {
                Files.walk(PROXY_PATH).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }

            if (platformInfo.extension.equals("tar.gz")) {
                try (TarInputStream tar = new TarInputStream(new GZIPInputStream(Files.newInputStream(zipFile)))) {
                    decompress(tar::getNextEntry, TestProxyDownloader::getTarOutputFile, TarEntry::isDirectory, tar);
                }
            } else {
                try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
                    decompress(zip::getNextEntry, TestProxyDownloader::getZipOuptutFile, ZipEntry::isDirectory, zip);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void decompress(Callable<T> entryGetter, Function<T, File> outputFileFunc,
        Predicate<T> isDirectory, InputStream archive) throws Exception {
        T entry = entryGetter.call();

        while (entry != null) {
            File outputFile = outputFileFunc.apply(entry);
            if (isDirectory.test(entry)) {
                if (!outputFile.isDirectory() && !outputFile.mkdirs()) {
                    throw new RuntimeException("Could not create all required directories");
                }
            } else {
                File parent = outputFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new RuntimeException("Could not create all required directories");
                }
                try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
                    TestingHelpers.copy(archive, outputStream);
                    if (outputFile.getName().equals(TestProxyUtils.getProxyProcessName())) {
                        outputFile.setExecutable(true, false);
                    }
                }
            }
            entry = entryGetter.call();
        }
    }

    private static File getZipOuptutFile(ZipEntry entry) {
        return new File(PROXY_PATH.toFile(), entry.getName());
    }

    private static File getTarOutputFile(TarEntry entry) {
        return new File(PROXY_PATH.toFile(), entry.getName());
    }

    private static void downloadProxy(PlatformInfo platformInfo) {
        LOGGER.log(LogLevel.INFORMATIONAL, () -> "Downloading test proxy. This may take a few moments.");

        try {
            URL url = UrlBuilder.parse(getProxyDownloadUrl(platformInfo)).toUrl();
            Files.copy(url.openStream(), getZipFileLocation(platformInfo.getExtension()),
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
            Files.write(filePath, Collections.singletonList(testProxyTag));
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
        return String.format(
            "https://github.com/Azure/azure-sdk-tools/releases/download/Azure.Sdk.Tools.TestProxy_%s/test-proxy-standalone-%s-%s.%s",
            testProxyTag, platformInfo.getPlatform(), platformInfo.getArchitecture(), platformInfo.getExtension());
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
            } else if (arch.contains("arm64") || arch.contains("aarch64")) {
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
