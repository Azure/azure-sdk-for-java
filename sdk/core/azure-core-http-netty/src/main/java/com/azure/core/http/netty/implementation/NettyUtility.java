// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.logging.LoggingEventBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.util.Version;
import reactor.netty.Connection;
import reactor.netty.channel.ChannelOperations;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Helper class containing utility methods.
 */
public final class NettyUtility {
    private static final ClientLogger LOGGER = new ClientLogger(NettyUtility.class);

    static final String PROPERTIES_FILE_NAME = "azure-core-http-netty.properties";
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
     * Closes a connection if it hasn't been disposed.
     *
     * @param reactorNettyConnection The connection to close.
     */
    public static void closeConnection(Connection reactorNettyConnection) {
        // ChannelOperations is generally the default implementation of Connection used.
        //
        // Using the specific subclass allows for a finer grain handling.
        if (reactorNettyConnection instanceof ChannelOperations) {
            ChannelOperations<?, ?> channelOperations = (ChannelOperations<?, ?>) reactorNettyConnection;

            // Given that this is an HttpResponse the only time this will be called is when the outbound has completed.
            //
            // From there the only thing that needs to be checked is whether the inbound has been disposed (completed),
            // and if not dispose it (aka drain it).
            if (!channelOperations.isInboundDisposed()) {
                channelOperations.channel().eventLoop().execute(channelOperations::discard);
            }
        } else if (!reactorNettyConnection.isDisposed()) {
            reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
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

        Map<String, String> pomVersions = CoreUtils.getProperties(PROPERTIES_FILE_NAME);
        NettyVersionLogInformation versionLogInformation = createNettyVersionLogInformation(
            pomVersions.get(NETTY_VERSION_PROPERTY), pomVersions.get(NETTY_TCNATIVE_VERSION_PROPERTY));
        if (versionLogInformation.shouldLog()) {
            versionLogInformation.log();
        }
    }

    static NettyVersionLogInformation createNettyVersionLogInformation(String azureNettyVersion,
        String azureNativeNettyVersion) {
        Map<String, String> classpathNettyVersions = new LinkedHashMap<>();
        Map<String, String> classPathNativeNettyVersions = new LinkedHashMap<>();

        Map<String, Version> nettyVersions = Version.identify();

        for (String artifact : REQUIRED_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as azure-core-http-netty has it as a dependency, but it could have been
            // excluded. Include it as a warning.
            if (version == null) {
                classpathNettyVersions.put("io.netty:" + artifact, "unknown (not found and is required)");
            } else {
                classpathNettyVersions.put("io.netty:" + version.artifactId(), version.artifactVersion());
            }
        }

        for (String artifact : OPTIONAL_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as azure-core-http-netty has it as a dependency, but it could have been
            // excluded. Don't include it as a warning for native dependencies as it is optional.
            if (version != null) {
                classpathNettyVersions.put("io.netty:" + version.artifactId(), version.artifactVersion());
            }
        }

        try {
            Enumeration<URL> enumeration = Thread.currentThread()
                .getContextClassLoader()
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

        return new NettyVersionLogInformation(azureNettyVersion, azureNativeNettyVersion, classpathNettyVersions,
            classPathNativeNettyVersions);
    }

    static final class NettyVersionLogInformation {
        private final String azureNettyVersion;
        private final String azureNativeNettyVersion;
        final Map<String, String> classpathNettyVersions;
        final Map<String, String> classPathNativeNettyVersions;

        NettyVersionLogInformation(String azureNettyVersion, String azureNativeNettyVersion,
            Map<String, String> classpathNettyVersions, Map<String, String> classPathNativeNettyVersions) {
            this.azureNettyVersion = azureNettyVersion;
            this.azureNativeNettyVersion = azureNativeNettyVersion;
            this.classpathNettyVersions = classpathNettyVersions;
            this.classPathNativeNettyVersions = classPathNativeNettyVersions;
        }

        boolean shouldLog() {
            boolean hasNettyVersionMismatch = classpathNettyVersions.values()
                .stream()
                .anyMatch(version -> !Objects.equals(version, azureNettyVersion));
            boolean hasNativeNettyVersionMismatch = classPathNativeNettyVersions.values()
                .stream()
                .anyMatch(version -> !Objects.equals(version, azureNativeNettyVersion));

            return hasNettyVersionMismatch || hasNativeNettyVersionMismatch;
        }

        private void log() {
            LoggingEventBuilder loggingEventBuilder = LOGGER.atInfo();

            loggingEventBuilder.addKeyValue("azure-netty-version", azureNettyVersion)
                .addKeyValue("azure-netty-native-version", azureNativeNettyVersion);

            for (Map.Entry<String, String> entry : classpathNettyVersions.entrySet()) {
                loggingEventBuilder.addKeyValue("classpath-netty-version-" + entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : classPathNativeNettyVersions.entrySet()) {
                loggingEventBuilder.addKeyValue("classpath-native-netty-version-" + entry.getKey(), entry.getValue());
            }

            loggingEventBuilder.log("The following Netty versions were found on the classpath and have a mismatch with "
                + "the versions used by azure-core-http-netty. If your application runs without issue this message "
                + "can be ignored, otherwise please align the Netty versions used in your application. For more "
                + "information, see https://aka.ms/azsdk/java/dependency/troubleshoot.");
        }
    }

    private NettyUtility() {
    }
}
