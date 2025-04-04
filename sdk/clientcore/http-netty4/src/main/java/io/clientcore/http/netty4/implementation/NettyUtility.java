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
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Helper class containing utility methods.
 */
public final class NettyUtility {
    private static final ClientLogger LOGGER = new ClientLogger(NettyUtility.class);

    static final String PROPERTIES_FILE_NAME = "http-netty.properties";
    static final String NETTY_VERSION_PROPERTY = "netty-version";
    static final String NETTY_TCNATIVE_VERSION_PROPERTY = "netty-tcnative-version";

    // List of Netty artifacts that should match the 'netty.version' property in the pom.xml file.
    // Non-native dependencies are required while native dependencies are optional. Without the native dependencies
    // the SDK will fall back to using the JDK implementations.
    private static final List<String> REQUIRED_NETTY_VERSION_ARTIFACTS = Arrays.asList("netty-common", "netty-handler",
        "netty-handler-proxy", "netty-buffer", "netty-codec", "netty-codec-http", "netty-codec-http2");
    private static final List<String> OPTIONAL_NETTY_VERSION_ARTIFACTS = Arrays
        .asList("netty-transport-native-unix-common", "netty-transport-native-epoll", "netty-transport-native-kqueue");

    // Netty artifact that should match the 'netty-tcnative.version' property in the pom.xml file.
    private static final String NETTY_TCNATIVE_VERSION_ARTIFACT = "netty-tcnative-boringssl-static";

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
     * Writes and releases all the eager {@link HttpContent} received in {@link CoreResponseHandler}.
     *
     * @param eagerContents The eager {@link HttpContent} to write to the stream.
     * @param outputStream The stream to write the eager {@link HttpContent} to.
     */
    static void writeEagerContentsToStreamAndRelease(List<HttpContent> eagerContents, OutputStream outputStream)
        throws IOException {
        for (int i = 0; i < eagerContents.size(); i++) {
            HttpContent eagerContent = eagerContents.get(i);
            if (eagerContent.content() == null) {
                ReferenceCountUtil.release(eagerContent);
                continue;
            }
            try {
                eagerContent.content().readBytes(outputStream, eagerContent.content().readableBytes());
            } catch (IOException ex) {
                for (; i < eagerContents.size(); i++) {
                    ReferenceCountUtil.release(eagerContents.get(i));
                }
                throw ex;
            } finally {
                ReferenceCountUtil.release(eagerContent);
            }
        }
    }

    /**
     * Deep copies the passed {@link ByteBuf} into a {@link ByteBuffer}.
     * <p>
     * Using this method ensures that data returned by the network is resilient against Reactor Netty releasing the
     * passed {@link ByteBuf} once the {@code doOnNext} operator fires.
     *
     * @param byteBuf The Netty {@link ByteBuf} to deep copy.
     * @return A newly allocated {@link ByteBuffer} containing the copied bytes.
     */
    public static ByteBuffer deepCopyBuffer(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        return ByteBuffer.wrap(bytes);
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
                .log("Unable to load Netty version information. If Netty version validation is required, "
                    + "please review this exception. Otherwise, this log message can be ignored.", ex);
        }
    }

    static void validateNettyVersionsInternal() {
        Map<String, String> pomVersions = CoreUtils.getProperties(PROPERTIES_FILE_NAME);
        NettyVersionLogInformation versionLogInformation = createNettyVersionLogInformation(
            pomVersions.get(NETTY_VERSION_PROPERTY), pomVersions.get(NETTY_TCNATIVE_VERSION_PROPERTY));
        if (versionLogInformation.shouldLog()) {
            versionLogInformation.log();
        }
    }

    static NettyVersionLogInformation createNettyVersionLogInformation(String nettyVersion, String nativeNettyVersion) {
        Map<String, String> classpathNettyVersions = new LinkedHashMap<>();
        Map<String, String> classPathNativeNettyVersions = new LinkedHashMap<>();

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

        try {
            Enumeration<URL> enumeration = NettyUtility.class.getClassLoader()
                .getResources("META-INF/maven/io.netty/netty-tcnative-boringssl-static/pom.properties");
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                Properties properties = new Properties();
                properties.load(url.openStream());

                String version = properties.getProperty("version");
                String groupId = properties.getProperty("groupId");
                String artifactId = properties.getProperty("artifactId");

                if ("io.netty".equals(groupId) && NETTY_TCNATIVE_VERSION_ARTIFACT.equals(artifactId)) {
                    classPathNativeNettyVersions.put("io.netty:" + NETTY_TCNATIVE_VERSION_ARTIFACT, version);
                }
            }
        } catch (IOException ignored) {
            // Ignored as this is only used to check the version of the native dependencies.
        }

        return new NettyVersionLogInformation(nettyVersion, nativeNettyVersion, classpathNettyVersions,
            classPathNativeNettyVersions);
    }

    static final class NettyVersionLogInformation {
        private final String nettyVersion;
        private final String nativeNettyVersion;
        final Map<String, String> classpathNettyVersions;
        final Map<String, String> classPathNativeNettyVersions;

        NettyVersionLogInformation(String nettyVersion, String nativeNettyVersion,
            Map<String, String> classpathNettyVersions, Map<String, String> classPathNativeNettyVersions) {
            this.nettyVersion = nettyVersion;
            this.nativeNettyVersion = nativeNettyVersion;
            this.classpathNettyVersions = classpathNettyVersions;
            this.classPathNativeNettyVersions = classPathNativeNettyVersions;
        }

        boolean shouldLog() {
            boolean hasNettyVersionMismatch
                = classpathNettyVersions.values().stream().anyMatch(version -> !Objects.equals(version, nettyVersion));
            boolean hasNativeNettyVersionMismatch = classPathNativeNettyVersions.values()
                .stream()
                .anyMatch(version -> !Objects.equals(version, nativeNettyVersion));

            return hasNettyVersionMismatch || hasNativeNettyVersionMismatch;
        }

        private void log() {
            LoggingEvent loggingEvent = LOGGER.atInfo();

            loggingEvent.addKeyValue("netty-version", nettyVersion)
                .addKeyValue("netty-native-version", nativeNettyVersion);

            for (Map.Entry<String, String> entry : classpathNettyVersions.entrySet()) {
                loggingEvent.addKeyValue("classpath-netty-version-" + entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : classPathNativeNettyVersions.entrySet()) {
                loggingEvent.addKeyValue("classpath-native-netty-version-" + entry.getKey(), entry.getValue());
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
