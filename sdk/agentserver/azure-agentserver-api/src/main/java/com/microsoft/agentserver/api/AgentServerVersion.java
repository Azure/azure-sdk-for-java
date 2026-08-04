// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

/**
 * Provides the {@code x-platform-server} header value identifying this library.
 * <p>
 * The header value is computed once at class initialization from the library version
 * and the current JVM version, e.g.:
 * {@code azure-ai-agentserver-java/1.0.0 (java/21)}
 */
public final class AgentServerVersion {

    private static final AgentServerVersion INSTANCE = new AgentServerVersion();

    private final String composedHeader;

    private AgentServerVersion() {
        String javaVersion = System.getProperty("java.version", "unknown");
        composedHeader = "azure-ai-agentserver-java/" + getLibraryVersion() + " (java/" + javaVersion + ")";
    }

    /**
     * Returns the singleton instance.
     */
    public static AgentServerVersion getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the {@code x-platform-server} header value for this library.
     */
    public String getHeaderValue() {
        return composedHeader;
    }

    private static String getLibraryVersion() {
        Package pkg = AgentServerVersion.class.getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        }
        return "1.0.0-SNAPSHOT";
    }
}
