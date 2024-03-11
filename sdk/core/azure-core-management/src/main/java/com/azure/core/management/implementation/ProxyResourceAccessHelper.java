// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.management.implementation;

import com.azure.core.management.ProxyResource;

/**
 * The helper class to access private members in {@link ProxyResource}.
 */
public final class ProxyResourceAccessHelper {
    private static ProxyResourceAccessor accessor;

    /**
     * Type defining the methods to set the non-public properties of a {@link ProxyResource} instance.
     */
    public interface ProxyResourceAccessor {
        /**
         * Sets the {@code id} property of the {@code proxyResource} instance.
         *
         * @param proxyResource the proxy resource instance whose {@code id} property is to be set.
         * @param id the id value.
         */
        void setId(ProxyResource proxyResource, String id);

        /**
         * Sets the {@code name} property of the {@code proxyResource} instance.
         *
         * @param proxyResource the proxy resource instance whose {@code name} property is to be set.
         * @param name the name value.
         */
        void setName(ProxyResource proxyResource, String name);

        /**
         * Sets the {@code type} property of the {@code proxyResource} instance.
         *
         * @param proxyResource the proxy resource instance whose {@code type} property is to be set.
         * @param type the type value.
         */
        void setType(ProxyResource proxyResource, String type);
    }

    /**
     * The method called from {@link ProxyResource} to set it's accessor.
     *
     * @param proxyResourceAccessor the accessor.
     */
    public static void setAccessor(final ProxyResourceAccessor proxyResourceAccessor) {
        accessor = proxyResourceAccessor;
    }

    /**
     * Sets the {@code id} property of the {@code proxyResource} instance.
     *
     * @param proxyResource the proxy resource instance whose {@code id} property is to be set.
     * @param id the id value.
     */
    public static void setId(ProxyResource proxyResource, String id) {
        ensureAccessor();

        accessor.setId(proxyResource, id);
    }

    /**
     * Sets the {@code name} property of the {@code proxyResource} instance.
     *
     * @param proxyResource the proxy resource instance whose {@code name} property is to be set.
     * @param name the name value.
     */
    public static void setName(ProxyResource proxyResource, String name) {
        ensureAccessor();

        accessor.setName(proxyResource, name);
    }

    /**
     * Sets the {@code type} property of the {@code proxyResource} instance.
     *
     * @param proxyResource the proxy resource instance whose {@code type} property is to be set.
     * @param type the type value.
     */
    public static void setType(ProxyResource proxyResource, String type) {
        ensureAccessor();

        accessor.setType(proxyResource, type);
    }

    private static void ensureAccessor() {
        // This will ensure that the accessor has been set before attempting to use it.
        if (accessor == null) {
            new ProxyResource();
        }
    }

    private ProxyResourceAccessHelper() {
    }
}
