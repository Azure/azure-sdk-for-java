// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.instrumentation.logging.LoggingEvent;
import io.clientcore.core.utils.CoreUtils;
import io.netty.buffer.ByteBuf;
import io.netty.util.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * Helper class containing utility methods.
 */
public final class NettyUtility {
    private static final ClientLogger LOGGER = new ClientLogger(NettyUtility.class);

    static final String PROPERTIES_FILE_NAME = "http-netty.properties";
    static final String NETTY_VERSION_PROPERTY = "netty-version";

    // List of Netty artifacts that should match the 'netty.version' property in the pom.xml file.
    // Non-native dependencies are required while native dependencies are optional. Without the native dependencies
    // the SDK will fall back to using the JDK implementations.
    private static final List<String> REQUIRED_NETTY_VERSION_ARTIFACTS
        = Arrays.asList("netty-buffer", "netty-codec", "netty-codec-http", "netty-codec-http2", "netty-common",
            "netty-handler", "netty-handler-proxy", "netty-resolver", "netty-resolver-dns", "netty-transport");
    private static final List<String> OPTIONAL_NETTY_VERSION_ARTIFACTS = Arrays
        .asList("netty-transport-native-unix-common", "netty-transport-native-epoll", "netty-transport-native-kqueue");

    /**
     * Converts Netty HttpHeaders to ClientCore HttpHeaders.
     * <p>
     * Most Netty requests should store headers in {@link WrappedHttpHeaders}, but if that doesn't happen this method
     * can be used to convert the Netty headers to ClientCore headers.
     *
     * @param nettyHeaders Netty HttpHeaders.
     * @return Converted ClientCore HttpHeaders.
     */
    public static HttpHeaders convertHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        int initialSize = (int) (nettyHeaders.size() / 0.75f);
        HttpHeaders coreHeaders = new HttpHeaders(initialSize);

        // Use Netty's iteratorAsString() to get the headers as a iterator of key-value pairs.
        // This is preferred over using Netty's names() and getAll(String) methods as those result in multiple scans
        // over the Netty headers.
        // This does result in more calls to HttpHeaderName.fromString(String) though, but in most cases this is
        // preferable as it is rare to have two headers with the same name, which is the only case where we'd see more
        // calls than necessary.
        nettyHeaders.iteratorAsString()
            .forEachRemaining(entry -> coreHeaders.add(HttpHeaderName.fromString(entry.getKey()), entry.getValue()));
        return coreHeaders;
    }

    /**
     * Awaits for the passed {@link CountDownLatch} to reach zero.
     *
     * @param latch The latch to wait for.
     */
    static void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for latch", e);
        }
    }

    /**
     * Reads the contents of the {@link ByteBuf} into an {@link OutputStream}.
     * <p>
     * Content will only be written to the {@link OutputStream} if the {@link ByteBuf} is non-null and is
     * {@link ByteBuf#isReadable()}. The entire {@link ByteBuf} will be consumed.
     *
     * @param byteBuf The Netty {@link ByteBuf} to read from.
     * @param stream The {@link OutputStream} to write to.
     * @throws NullPointerException If {@code stream} is null.
     * @throws IOException If an I/O error occurs.
     */
    static void readByteBufIntoOutputStream(ByteBuf byteBuf, OutputStream stream) throws IOException {
        Objects.requireNonNull(stream, "Cannot read a ByteBuf into a null 'stream'.");
        if (byteBuf == null || !byteBuf.isReadable()) {
            return;
        }

        byteBuf.readBytes(stream, byteBuf.readableBytes());
        if (byteBuf.refCnt() > 0) {
            // Release the ByteBuf as we've consumed it.
            byteBuf.release();
        }
    }

    /**
     * Checks the runtime version of the Netty libraries.
     * <p>
     * If the runtime versions match the dependencies in the pom.xml file, this method will do nothing.
     * <p>
     * If the runtime versions do not match the dependencies in the pom.xml file, this method will log a warning. The
     * warning will contain the versions found in runtime and the expected versions to be used by the SDK.
     */
    public static void validateNettyVersions() {
        if (!LOGGER.canLogAtLevel(LogLevel.INFORMATIONAL)) {
            return;
        }

        try {
            validateNettyVersionsInternal();
        } catch (Exception ex) {
            LOGGER.atInfo()
                .setThrowable(ex)
                .log("Unable to load Netty version information. If Netty version validation is required, "
                    + "please review this exception. Otherwise, this log message can be ignored.");
        }
    }

    static void validateNettyVersionsInternal() {
        Map<String, String> pomVersions = CoreUtils.getProperties(PROPERTIES_FILE_NAME);
        NettyVersionLogInformation versionLogInformation
            = createNettyVersionLogInformation(pomVersions.get(NETTY_VERSION_PROPERTY));
        if (versionLogInformation.shouldLog()) {
            versionLogInformation.log();
        }
    }

    static NettyVersionLogInformation createNettyVersionLogInformation(String nettyVersion) {
        Map<String, String> classpathNettyVersions = new LinkedHashMap<>();

        Map<String, Version> nettyVersions = Version.identify(NettyUtility.class.getClassLoader());

        for (String artifact : REQUIRED_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as http-netty4 has it as a dependency, but it could have been
            // excluded. Include it as a warning.
            if (version == null) {
                classpathNettyVersions.put("io.netty:" + artifact, "unknown (not found and is required)");
            } else {
                classpathNettyVersions.put("io.netty:" + version.artifactId(), version.artifactVersion());
            }
        }

        for (String artifact : OPTIONAL_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as http-netty4 has it as a dependency, but it could have been
            // excluded. Don't include it as a warning for native dependencies as it is optional.
            if (version != null) {
                classpathNettyVersions.put("io.netty:" + version.artifactId(), version.artifactVersion());
            }
        }

        return new NettyVersionLogInformation(nettyVersion, classpathNettyVersions);
    }

    static final class NettyVersionLogInformation {
        private final String nettyVersion;
        final Map<String, String> classpathNettyVersions;

        NettyVersionLogInformation(String nettyVersion, Map<String, String> classpathNettyVersions) {
            this.nettyVersion = nettyVersion;
            this.classpathNettyVersions = classpathNettyVersions;
        }

        boolean shouldLog() {
            return classpathNettyVersions.values().stream().anyMatch(version -> !Objects.equals(version, nettyVersion));
        }

        private void log() {
            LoggingEvent loggingEvent = LOGGER.atInfo().addKeyValue("netty-version", nettyVersion);

            for (Map.Entry<String, String> entry : classpathNettyVersions.entrySet()) {
                loggingEvent.addKeyValue("classpath-netty-version-" + entry.getKey(), entry.getValue());
            }

            loggingEvent.log("The following Netty versions were found on the classpath and have a mismatch with "
                + "the versions used by http-netty. If your application runs without issue this message can be "
                + "ignored, otherwise please align the Netty versions used in your application. For more information, "
                + "see https://aka.ms/azsdk/java/dependency/troubleshoot.");
        }
    }

    private NettyUtility() {
    }
}
