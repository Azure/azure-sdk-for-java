// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.netty.util.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Information about the versions of Netty libraries used at runtime.
 */
public final class NettyVersion {
    private static final ClientLogger LOGGER = new ClientLogger(NettyVersion.class);

    private static final String PROPERTIES_FILE_NAME = "azure-core-http-netty.properties";
    private static final String NETTY_VERSION_PROPERTY = "netty-version";
    private static final String NETTY_TCNATIVE_VERSION_PROPERTY = "netty-tcnative-version";

    // List of Netty artifacts that should match the 'netty.version' property in the pom.xml file.
    // Non-native dependencies are required while native dependencies are optional. Without the native dependencies
    // the SDK will fall back to using the JDK implementations.
    private static final List<String> REQUIRED_NETTY_VERSION_ARTIFACTS = Arrays.asList("netty-common", "netty-handler",
        "netty-handler-proxy", "netty-buffer", "netty-codec", "netty-codec-http", "netty-codec-http2");
    private static final List<String> OPTIONAL_NETTY_VERSION_ARTIFACTS = Arrays.asList(
        "netty-transport-native-unix-common", "netty-transport-native-epoll", "netty-transport-native-kqueue");

    // List of Netty artifacts that should match the 'netty-tcnative.version' property in the pom.xml file.
    private static final List<String> NETTY_TCNATIVE_VERSION_ARTIFACTS = Collections.singletonList(
        "netty-tcnative-boringssl-static");

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

    private NettyVersion() {
    }
}
