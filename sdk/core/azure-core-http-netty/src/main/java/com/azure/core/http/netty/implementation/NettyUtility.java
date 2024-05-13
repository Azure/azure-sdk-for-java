// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import io.netty.buffer.ByteBuf;
import io.netty.util.Version;
import reactor.netty.Connection;
import reactor.netty.channel.ChannelOperations;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Helper class containing utility methods.
 */
public final class NettyUtility {
    private static final ClientLogger LOGGER = new ClientLogger(NettyUtility.class);

    private static final String PROPERTIES_FILE_NAME = "azure-core-http-netty.properties";
    private static final String NETTY_VERSION_PROPERTY = "netty-version";
    private static final String NETTY_TCNATIVE_VERSION_PROPERTY = "netty-tcnative-version";

    // List of Netty artifacts that should match the 'netty.version' property in the pom.xml file.
    // Non-native dependencies are required while native dependencies are optional. Without the native dependencies
    // the SDK will fall back to using the JDK implementations.
    private static final List<String> REQUIRED_NETTY_VERSION_ARTIFACTS = Arrays.asList("netty-common", "netty-handler",
        "netty-handler-proxy", "netty-buffer", "netty-codec", "netty-codec-http", "netty-codec-http2");
    private static final List<String> OPTIONAL_NETTY_VERSION_ARTIFACTS = Arrays
        .asList("netty-transport-native-unix-common", "netty-transport-native-epoll", "netty-transport-native-kqueue");

    // List of Netty artifacts that should match the 'netty-tcnative.version' property in the pom.xml file.
    private static final List<String> NETTY_TCNATIVE_VERSION_ARTIFACTS
        = Collections.singletonList("netty-tcnative-boringssl-static");

    static final String NETTY_VERSION_MISMATCH_LOG = "The versions of Netty being used are not aligned. ";

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
        if (LOGGER.canLogAtLevel(LogLevel.INFORMATIONAL)) {
            validateNettyVersions(LOGGER::info);
        }
    }

    static void validateNettyVersions(Consumer<String> logger) {
        Map<String, String> pomVersions = CoreUtils.getProperties(PROPERTIES_FILE_NAME);
        String nettyVersion = pomVersions.get(NETTY_VERSION_PROPERTY);
        String nettyTcnativeVersion = pomVersions.get(NETTY_TCNATIVE_VERSION_PROPERTY);

        Map<String, Version> nettyVersions = Version.identify();
        List<String> versionInformation = new ArrayList<>(11); // There are 11 Netty dependencies in the pom.xml file.

        Set<String> nonNativeNettyVersions = new HashSet<>();
        for (String artifact : REQUIRED_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as azure-core-http-netty has it as a dependency, but it could have been
            // excluded. Include it as a warning.
            if (version == null) {
                versionInformation.add("'io.netty:" + artifact + "' (not found and is required)");
                nonNativeNettyVersions.add("unknown");
            } else {
                versionInformation.add("'io.netty:" + artifact + "' version: " + version.artifactVersion());
                nonNativeNettyVersions.add(version.artifactVersion());
            }
        }

        for (String artifact : OPTIONAL_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as azure-core-http-netty has it as a dependency, but it could have been
            // excluded. Don't include it as a warning for native dependencies as it is optional.
            if (version != null) {
                versionInformation.add("'io.netty:" + artifact + "' version: " + version.artifactVersion());
                nonNativeNettyVersions.add(version.artifactVersion());
            }
        }

        for (String artifact : NETTY_TCNATIVE_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as azure-core-http-netty has it as a dependency, but it could have been
            // excluded. Don't include it as a warning for native dependencies as it is optional.
            if (version != null) {
                versionInformation.add("'io.netty:" + artifact + "' version: " + version.artifactVersion());
            }
        }

        String versionInformationString = CoreUtils.stringJoin(", ", versionInformation);
        StringBuilder stringBuilder
            = new StringBuilder().append("The following is Netty version information that was found on the classpath: ")
                .append(versionInformationString)
                .append(". ");

        if (nonNativeNettyVersions.size() > 1) {
            stringBuilder.append(NETTY_VERSION_MISMATCH_LOG);
        }

        stringBuilder.append("The version of azure-core-http-netty being used was built with Netty version ")
            .append(nettyVersion)
            .append(" and Netty Tcnative version ")
            .append(nettyTcnativeVersion)
            .append(". If your application runs without issue this message can be ignored, otherwise please align the "
                + "Netty versions used in your application. For more information, see "
                + "https://aka.ms/azsdk/java/dependency/troubleshoot.");

        logger.accept(stringBuilder.toString());
    }

    private NettyUtility() {
    }
}
