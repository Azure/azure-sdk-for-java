// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ProxyAuthenticator;

import java.util.List;
import java.util.Map;

/**
 * Access helper to provide access to the package-private constructor of {@link ProxyAuthenticator.ChallengeResponse}.
 */
public final class ChallengeResponseAccessHelper {
    private static ChallengeResponseAccessor accessor;

    /**
     * The {@link ProxyAuthenticator.ChallengeResponse} accessor contract.
     */
    public interface ChallengeResponseAccessor {
        /**
         * Creates a new instance of {@link ProxyAuthenticator.ChallengeResponse} using its package-private constructor.
         *
         * @param headers the proxy challenge response headers.
         * @return the created instance.
         */
        ProxyAuthenticator.ChallengeResponse internalCreate(Map<String, List<String>> headers);
    }

    /**
     * Sets the accessor.
     *
     * @param accessor the accessor.
     */
    public static void setAccessor(ChallengeResponseAccessor accessor) {
        ChallengeResponseAccessHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ProxyAuthenticator.ChallengeResponse} using its package-private constructor.
     *
     * @param headers the proxy challenge response headers.
     * @return the created instance.
     * @throws RuntimeException if the accessor lookup fails.
     */
    public static ProxyAuthenticator.ChallengeResponse internalCreate(Map<String, List<String>> headers) {
        if (accessor == null) {
            try {
                Class.forName(ProxyAuthenticator.ChallengeResponse.class.getName(), true,
                    ChallengeResponseAccessHelper.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        assert accessor != null;
        return accessor.internalCreate(headers);
    }

    /**
     * Access helper shouldn't have an accessible constructor.
     */
    private ChallengeResponseAccessHelper() {
    }
}
