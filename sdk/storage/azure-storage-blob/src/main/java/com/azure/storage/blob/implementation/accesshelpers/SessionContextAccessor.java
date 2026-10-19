// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.SessionContext;

import java.util.function.Supplier;

/**
 * Provides internal access to session acquisition configuration without adding provider types to the public API.
 */
public final class SessionContextAccessor {
    private static volatile SessionContextAccessorImpl accessor;

    private SessionContextAccessor() {
    }

    /**
     * Internal bridge implemented by {@link SessionContext}.
     */
    public interface SessionContextAccessorImpl {
        String getEndpoint(SessionContext context);

        TokenCredential getCredential(SessionContext context);

        <T> T getOrCreateSessionProvider(SessionContext context, Class<T> providerType,
            Supplier<? extends T> providerFactory);
    }

    /**
     * Registers the accessor implemented by {@link SessionContext}.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(SessionContextAccessorImpl accessor) {
        SessionContextAccessor.accessor = accessor;
    }

    /**
     * Gets the session acquisition endpoint.
     *
     * @param context Session context.
     * @return The acquisition endpoint, or {@code null} when the options use the consuming client's endpoint.
     */
    public static String getEndpoint(SessionContext context) {
        return getAccessor().getEndpoint(context);
    }

    /**
     * Gets the credential used for session acquisition.
     *
     * @param context Session context.
     * @return The acquisition credential, or {@code null} when the options use the consuming client's pipeline.
     */
    public static TokenCredential getCredential(SessionContext context) {
        return getAccessor().getCredential(context);
    }

    /**
     * Gets or creates the session provider owned by the context instance.
     *
     * @param context Session context.
     * @param providerType Type of the internal session provider.
     * @param providerFactory Factory used to create the provider once.
     * @param <T> Internal session provider type.
     * @return The existing or newly created provider.
     */
    public static <T> T getOrCreateSessionProvider(SessionContext context, Class<T> providerType,
        Supplier<? extends T> providerFactory) {
        return getAccessor().getOrCreateSessionProvider(context, providerType, providerFactory);
    }

    private static SessionContextAccessorImpl getAccessor() {
        if (accessor == null) {
            try {
                Class.forName(SessionContext.class.getName(), true, SessionContext.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return accessor;
    }
}
