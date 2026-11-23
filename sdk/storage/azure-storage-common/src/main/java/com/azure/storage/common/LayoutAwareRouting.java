// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

/**
 * Defines the routing behavior the SDK should use when download hint metadata indicates blob layout is available.
 * <p>
 * This is a performance optimization only. The bytes returned are identical regardless of the mode used.
 * Client-side encrypted blob clients do not support layout-aware routing and always use their configured endpoint. A
 * warning is logged when {@link #ENABLED} is explicitly requested for an unsupported client.
 */
public enum LayoutAwareRouting {
    /**
     * Let the SDK choose the routing behavior most appropriate for the request.
     * <p>
     * The behavior selected by {@code AUTO} is an implementation detail and may change between SDK releases.
     * Choose {@link #DISABLED} or {@link #ENABLED} explicitly if you require a specific behavior.
     */
    AUTO,

    /**
     * Never use layout-aware routing. The client always uses its configured endpoint.
     */
    DISABLED,

    /**
     * Opt in to layout-aware routing. The client fetches blob layout on demand, caches it, and refreshes it in the
     * background when needed.
     * <p>
     * Layout-aware routing connects to the selected locality endpoint while preserving the original storage account
     * authority in the {@code Host} header. When using the JDK HTTP transport, start the JVM with
     * {@code -Djdk.httpclient.allowRestrictedHeaders=host} before the JDK HTTP client is initialized. If the property
     * already specifies other headers, add {@code host} to its comma-separated list. Do not configure the JDK HTTP
     * transport to use HTTP/2 for layout-aware routing, as its {@code :authority} remains derived from the request
     * URL. This setting affects all JDK HTTP clients in the process. See the
     * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-jdk-httpclient/README.md#request-host-overrides">JDK
     * HTTP transport documentation</a> for custom configuration and other limitations.
     */
    ENABLED
}
