// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class for managing downloads of the test proxy
 */
public final class TestProxyDownloader {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyDownloader.class);
    private static final Path HOME_PATH = Paths.get(System.getProperty("user.home")).toAbsolutePath().normalize();
    private static final Path SDK_CACHE_PATH = HOME_PATH.resolve(".azure-sdk-for-java");
    private static final Path CACHE_PATH = SDK_CACHE_PATH.resolve("test-proxy");
    private static final Path PROXY_PATH = CACHE_PATH.resolve("bin");
    private static final Set<PosixFilePermission> PRIVATE_PERMISSIONS = EnumSet.of(PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);

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
     * @throws UncheckedIOException If the cache is unsafe or the installation cannot be verified.
     */
    public static synchronized void installTestProxy(Path testClassPath) {
        testProxyTag = TestProxyUtils.getTestProxyVersion(testClassPath);
        try {
            ensurePrivateCache();
            try (FileChannel channel = FileChannel.open(CACHE_PATH.resolve("install.lock"), StandardOpenOption.CREATE,
                StandardOpenOption.WRITE); FileLock lock = channel.lock()) {
                if (!lock.isValid()) {
                    throw LOGGER.logExceptionAsError(
                        new UncheckedIOException(new IOException("The test proxy cache lock is not valid.")));
                }
                if (!checkDownloadedVersion()) {
                    PlatformInfo platformInfo = new PlatformInfo();
                    downloadProxy(platformInfo);
                    extractTestProxy(platformInfo);
                    updateDownloadedFileVersion();
                }
            }
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException("Could not install a verified test proxy.", e));
        }
    }

    static Path getProxyExecutable() {
        try {
            ensurePrivateCache();
            if (!checkDownloadedVersion()) {
                throw new IOException("The requested test proxy version is not installed.");
            }
            return PROXY_PATH.resolve(TestProxyUtils.getProxyProcessName());
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException("Could not verify the cached test proxy.", e));
        }
    }

    private static void ensurePrivateCache() throws IOException {
        ensurePrivateHome(HOME_PATH);
        ensurePrivateDirectory(SDK_CACHE_PATH);
        ensurePrivateDirectory(CACHE_PATH);
    }

    /*
     * Ownership and permissions are only checked on POSIX file systems. On other file systems, such as Windows, the
     * user profile is protected by ACLs and is commonly owned by SYSTEM or Administrators rather than the user.
     */
    static void ensurePrivateHome(Path home) throws IOException {
        if (!isPosix(home)) {
            return;
        }
        Path realHome = home.toRealPath();
        if (!Files.getOwner(realHome).equals(getCurrentUser(realHome))) {
            throw new IOException("The test proxy cache must be under the current user's home directory.");
        }
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(realHome);
        if (permissions.contains(PosixFilePermission.GROUP_WRITE)
            || permissions.contains(PosixFilePermission.OTHERS_WRITE)) {
            throw new IOException("The test proxy home directory must not be writable by other users.");
        }
    }

    static void ensurePrivateDirectory(Path directory) throws IOException {
        boolean posix = isPosix(directory);
        try {
            if (posix) {
                Files.createDirectory(directory, PosixFilePermissions.asFileAttribute(PRIVATE_PERMISSIONS));
            } else {
                Files.createDirectory(directory);
            }
        } catch (FileAlreadyExistsException e) {
            if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("The test proxy cache must not be a symbolic link or a non-directory.", e);
            }
        }
        if (!posix) {
            return;
        }
        if (!Files.getOwner(directory, LinkOption.NOFOLLOW_LINKS).equals(getCurrentUser(directory))
            || !Files.getPosixFilePermissions(directory, LinkOption.NOFOLLOW_LINKS).equals(PRIVATE_PERMISSIONS)) {
            throw new IOException("The test proxy cache must be owned and accessible only by the current user.");
        }
    }

    private static boolean isPosix(Path path) {
        return Files.getFileAttributeView(path, PosixFileAttributeView.class) != null;
    }

    private static UserPrincipal getCurrentUser(Path path) throws IOException {
        return path.getFileSystem()
            .getUserPrincipalLookupService()
            .lookupPrincipalByName(System.getProperty("user.name"));
    }

    private static void extractTestProxy(PlatformInfo platformInfo) {
        Path zipFile = getZipFileLocation(platformInfo.getExtension());
        try {
            extractVerifiedArchive(zipFile, PROXY_PATH, platformInfo.extension.equals("tar.gz"));
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Could not extract the verified test proxy.", e));
        }
    }

    static void extractVerifiedArchive(Path archive, Path installation, boolean tarGz) throws Exception {
        if (Files.isSymbolicLink(installation)) {
            throw new IOException("The test proxy installation must not be a symbolic link.");
        }
        if (Files.exists(installation)) {
            try (Stream<Path> paths = Files.walk(installation)) {
                Iterator<Path> iterator = paths.sorted(Comparator.reverseOrder()).iterator();
                while (iterator.hasNext()) {
                    Files.delete(iterator.next());
                }
            }
        }
        if (tarGz) {
            try (TarInputStream tar = new TarInputStream(new GZIPInputStream(Files.newInputStream(archive)))) {
                decompress(tar::getNextEntry, entry -> getArchiveOutputFile(installation, entry.getName()).toFile(),
                    TarEntry::isDirectory, tar, installation);
            }
        } else {
            try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(archive)))) {
                decompress(zip::getNextEntry, entry -> getArchiveOutputFile(installation, entry.getName()).toFile(),
                    ZipEntry::isDirectory, zip, installation);
            }
        }
    }

    private static <T> void decompress(Callable<T> entryGetter, Function<T, File> outputFileFunc,
        Predicate<T> isDirectory, InputStream archive, Path installation) throws Exception {
        T entry = entryGetter.call();

        while (entry != null) {
            File outputFile = outputFileFunc.apply(entry);
            if (isDirectory.test(entry)) {
                if (!outputFile.isDirectory() && !outputFile.mkdirs()) {
                    throw new RuntimeException("Could not create all required directories");
                }
            } else {
                if (outputFile.toPath().equals(installation)) {
                    throw new IOException("The test proxy archive contains an invalid file entry.");
                }
                File parent = outputFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new RuntimeException("Could not create all required directories");
                }
                try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
                    TestingHelpers.copy(archive, outputStream);
                    if (outputFile.getName().equals(TestProxyUtils.getProxyProcessName())) {
                        if (!outputFile.setExecutable(true, true)) {
                            throw new IOException("Could not make the test proxy executable.");
                        }
                    }
                }
            }
            entry = entryGetter.call();
        }
    }

    static Path getArchiveOutputFile(Path directory, String entryName) {
        Path output = directory.resolve(entryName).normalize();
        if (!output.startsWith(directory)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The test proxy archive contains an invalid entry path."));
        }
        return output;
    }

    private static void downloadProxy(PlatformInfo platformInfo) {
        LOGGER.log(LogLevel.INFORMATIONAL, () -> "Downloading test proxy. This may take a few moments.");

        try {
            String expectedDigest;
            URL releaseUrl = UrlBuilder
                .parse("https://api.github.com/repos/Azure/azure-sdk-tools/releases/tags/Azure.Sdk.Tools.TestProxy_"
                    + testProxyTag)
                .toUrl();
            URLConnection connection = releaseUrl.openConnection();
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("User-Agent", "azure-core-test");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(60_000);
            try (InputStream release = connection.getInputStream();
                JsonReader reader = JsonProviders.createReader(release)) {
                expectedDigest = readExpectedDigest(reader, platformInfo.getAssetName());
            }

            URL url = UrlBuilder.parse(getProxyDownloadUrl(platformInfo)).toUrl();
            try (InputStream archive = url.openStream()) {
                Files.copy(archive, getZipFileLocation(platformInfo.getExtension()),
                    StandardCopyOption.REPLACE_EXISTING);
            }
            verifyDigest(getZipFileLocation(platformInfo.getExtension()), expectedDigest);
        } catch (IOException e) {
            throw LOGGER
                .logExceptionAsError(new UncheckedIOException("Could not download and verify the test proxy.", e));
        }
    }

    private static Path getZipFileLocation(String extension) {
        return CACHE_PATH.resolve(String.format("testproxy.%s", extension));
    }

    private static void updateDownloadedFileVersion() throws IOException {
        Path filePath = getFileVersionPath();
        String executableDigest = sha256(PROXY_PATH.resolve(TestProxyUtils.getProxyProcessName()));
        Files.write(filePath, Arrays.asList(testProxyTag, executableDigest), StandardCharsets.UTF_8);
    }

    private static boolean checkDownloadedVersion() throws IOException {
        return checkDownloadedVersion(PROXY_PATH, getFileVersionPath(), testProxyTag);
    }

    static boolean checkDownloadedVersion(Path installation, Path filePath, String expectedVersion) throws IOException {
        if (!Files.exists(filePath, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }
        if (!Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("The test proxy version file must be a regular file.");
        }
        List<String> version = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        if (version.size() != 2) {
            throw new IOException("The cached test proxy integrity metadata is invalid.");
        }
        if (!version.get(0).equals(expectedVersion)) {
            return false;
        }
        if (!Files.isDirectory(installation, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("The test proxy installation must be a regular directory.");
        }
        verifyDigest(installation.resolve(TestProxyUtils.getProxyProcessName()), version.get(1));
        return true;
    }

    private static Path getFileVersionPath() {
        return CACHE_PATH.resolve("test-proxy-version.txt");
    }

    static String readExpectedDigest(JsonReader reader, String assetName) throws IOException {
        String digest = reader.readObject(release -> {
            String match = null;
            while (release.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = release.getFieldName();
                release.nextToken();
                if ("assets".equals(fieldName)) {
                    List<String> digests = release.readArray(asset -> readAssetDigest(asset, assetName));
                    if (digests != null) {
                        for (String candidate : digests) {
                            if (candidate != null) {
                                if (match != null) {
                                    throw new IOException("The test proxy release has duplicate assets.");
                                }
                                match = candidate;
                            }
                        }
                    }
                } else {
                    release.skipChildren();
                }
            }
            return match;
        });
        if (digest == null || !digest.matches("sha256:[0-9a-fA-F]{64}")) {
            throw new IOException("The test proxy release does not provide a valid SHA-256 digest for this platform.");
        }
        return digest.substring("sha256:".length()).toLowerCase(Locale.ROOT);
    }

    private static String readAssetDigest(JsonReader reader, String assetName) throws IOException {
        return reader.readObject(asset -> {
            String name = null;
            String digest = null;
            while (asset.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = asset.getFieldName();
                asset.nextToken();
                if ("name".equals(fieldName)) {
                    name = asset.getString();
                } else if ("digest".equals(fieldName)) {
                    digest = asset.getString();
                } else {
                    asset.skipChildren();
                }
            }
            return assetName.equals(name) ? digest : null;
        });
    }

    static void verifyDigest(Path file, String expectedDigest) throws IOException {
        if (!sha256(file).equalsIgnoreCase(expectedDigest)) {
            throw new IOException("The test proxy SHA-256 integrity verification failed.");
        }
    }

    private static String sha256(Path file) throws IOException {
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("The test proxy archive or executable must be a regular file.");
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("SHA-256 is required to verify test proxy.", e));
        }
        try (InputStream stream = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                digest.update(buffer, 0, count);
            }
        }
        return CoreUtils.bytesToHexString(digest.digest());
    }

    private static String getProxyDownloadUrl(PlatformInfo platformInfo) {
        return String.format(
            "https://github.com/Azure/azure-sdk-tools/releases/download/Azure.Sdk.Tools.TestProxy_%s/%s", testProxyTag,
            platformInfo.getAssetName());
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

        String getAssetName() {
            return String.format("test-proxy-standalone-%s-%s.%s", platform, architecture, extension);
        }
    }
}
