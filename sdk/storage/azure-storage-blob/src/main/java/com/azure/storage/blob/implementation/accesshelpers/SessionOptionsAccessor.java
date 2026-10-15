// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.options.SessionOptions;

import java.util.function.Supplier;

/**
 * Provides internal access to session acquisition configuration without adding provider types to the public API.
 */
public final class SessionOptionsAccessor {
    private static volatile SessionOptionsAccessorImpl accessor;

    private SessionOptionsAccessor() {
    }

    /**
     * Internal bridge implemented by {@link SessionOptions}.
     */
    public interface SessionOptionsAccessorImpl {
        String getEndpoint(SessionOptions options);

        TokenCredential getCredential(SessionOptions options);

        <T> T getOrCreateSessionClient(SessionOptions options, Class<T> clientType,
            Supplier<? extends T> clientFactory);
    }

    /**
     * Registers the accessor implemented by {@link SessionOptions}.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(SessionOptionsAccessorImpl accessor) {
        SessionOptionsAccessor.accessor = accessor;
    }

    /**
     * Gets the session acquisition endpoint.
     *
     * @param options Session options.
     * @return The acquisition endpoint, or {@code null} when the options use the consuming client's endpoint.
     */
    public static String getEndpoint(SessionOptions options) {
        return getAccessor().getEndpoint(options);
    }

    /**
     * Gets the credential used for session acquisition.
     *
     * @param options Session options.
     * @return The acquisition credential, or {@code null} when the options use the consuming client's pipeline.
     */
    public static TokenCredential getCredential(SessionOptions options) {
        return getAccessor().getCredential(options);
    }

    /**
     * Gets or creates the session provider owned by the options instance.
     *
     * @param options Session options.
     * @param clientType Type of the internal session provider.
     * @param clientFactory Factory used to create the provider once.
     * @param <T> Internal session provider type.
     * @return The existing or newly created provider.
     */
    public static <T> T getOrCreateSessionClient(SessionOptions options, Class<T> clientType,
        Supplier<? extends T> clientFactory) {
        return getAccessor().getOrCreateSessionClient(options, clientType, clientFactory);
    }

    private static SessionOptionsAccessorImpl getAccessor() {
        if (accessor == null) {
            try {
                Class.forName(SessionOptions.class.getName(), true, SessionOptions.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return accessor;
    }
}
