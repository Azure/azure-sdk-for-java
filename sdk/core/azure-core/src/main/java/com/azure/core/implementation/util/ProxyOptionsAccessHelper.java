// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.http.ProxyOptions;

/**
 * This class is used to access internal methods on {@link ProxyOptions}.
 */
public final class ProxyOptionsAccessHelper {
    private static ProxyOptionsAccessor accessor;

    /**
     * Type defining the methods to access the internal methods on {@link ProxyOptions}.
     */
    public interface ProxyOptionsAccessor {
        /**
         * Gets the version of the parser that converted the non-proxy hosts of {@link ProxyOptions} to a regex.
         *
         * @param proxyOptions The {@link ProxyOptions} to get the parser version from.
         * @return The parser version, 1 or 2. Version 1 is returned when no non-proxy hosts were set.
         */
        int getNonProxyHostsParserVersion(ProxyOptions proxyOptions);
    }

    /**
     * Gets the version of the parser that converted the non-proxy hosts of {@link ProxyOptions} to a regex.
     *
     * @param proxyOptions The {@link ProxyOptions} to get the parser version from.
     * @return The parser version, 1 or 2. Version 1 is returned when no non-proxy hosts were set.
     */
    public static int getNonProxyHostsParserVersion(ProxyOptions proxyOptions) {
        return accessor.getNonProxyHostsParserVersion(proxyOptions);
    }

    /**
     * Sets the {@link ProxyOptionsAccessor}.
     *
     * @param accessor The {@link ProxyOptionsAccessor}.
     */
    public static void setAccessor(ProxyOptionsAccessor accessor) {
        ProxyOptionsAccessHelper.accessor = accessor;
    }

    private ProxyOptionsAccessHelper() {
    }
}
