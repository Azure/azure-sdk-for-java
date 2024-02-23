// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.ByteBuf;
import io.netty.util.Version;
import reactor.netty.Connection;
import reactor.netty.channel.ChannelOperations;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Helper class containing utility methods.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

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
        ByteBuffer buffer = ByteBuffer.allocate(byteBuf.readableBytes());
        byteBuf.readBytes(buffer);
        buffer.rewind();
        return buffer;
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
        Map<String, String> pomVersions = CoreUtils.getProperties(PROPERTIES_FILE_NAME);
        String nettyVersion = pomVersions.get(NETTY_VERSION_PROPERTY);
        String nettyTcnativeVersion = pomVersions.get(NETTY_TCNATIVE_VERSION_PROPERTY);

        Map<String, Version> nettyVersions = Version.identify();
        List<String> versionMismatches = new ArrayList<>(11); // There are 11 Netty dependencies in the pom.xml file.

        for (String artifact : REQUIRED_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as azure-core-http-netty has it as a dependency, but it could have been
            // excluded. Include it as a warning.
            if (version == null) {
                versionMismatches.add("'io.netty:" + artifact + "' version not found (expected: " + nettyVersion + ")");
            } else if (!Objects.equals(version.artifactVersion(), nettyVersion)) {
                versionMismatches.add("'io.netty:" + artifact + "' version: " + version.artifactVersion()
                    + " (expected: " + nettyVersion + ")");
            }
        }

        for (String artifact : OPTIONAL_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as azure-core-http-netty has it as a dependency, but it could have been
            // excluded. Don't include it as a warning for native dependencies as it is optional.
            if (version != null && !Objects.equals(version.artifactVersion(), nettyVersion)) {
                versionMismatches.add("'io.netty:" + artifact + "' version: " + version.artifactVersion()
                    + " (expected: " + nettyVersion + ")");
            }
        }

        for (String artifact : NETTY_TCNATIVE_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as azure-core-http-netty has it as a dependency, but it could have been
            // excluded. Don't include it as a warning for native dependencies as it is optional.
            if (version != null && !Objects.equals(version.artifactVersion(), nettyTcnativeVersion)) {
                versionMismatches.add("'io.netty:" + artifact + "' version: " + version.artifactVersion()
                    + " (expected: " + nettyTcnativeVersion + ")");
            }
        }

        if (!versionMismatches.isEmpty()) {
            LOGGER.warning("The following Netty dependencies have versions that do not match the versions specified in "
                + "the azure-core-http-netty pom.xml file. This may result in unexpected behavior. If your application "
                + "runs without issue this message can be ignored, otherwise please update the Netty dependencies to "
                + "match the versions specified in the pom.xml file. Versions found in runtime: "
                + CoreUtils.stringJoin(",", versionMismatches));
        }
    }

    private Utility() {
    }
}
