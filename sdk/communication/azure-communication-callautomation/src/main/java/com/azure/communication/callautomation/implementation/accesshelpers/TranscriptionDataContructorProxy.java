// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.converters.TranscriptionDataConverter;
import com.azure.communication.callautomation.models.TranscriptionData;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper class to access private values of {@link TranscriptionData} across package boundaries.
 */
public final class TranscriptionDataContructorProxy {
    private static final ClientLogger LOGGER = new ClientLogger(TranscriptionDataContructorProxy.class);
    private static TranscriptionDataContructorProxyAccessor accessor;

    private TranscriptionDataContructorProxy() {
    }

    /**
    * Type defining the methods to set the non-public properties of a {@link TranscriptionDataContructorProxyAccessor}
    * instance.
    */
    public interface TranscriptionDataContructorProxyAccessor {
        /**
         * Creates a new instance of {@link TranscriptionData} backed by an internal instance of
         * {@link TranscriptionDataConverter}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link TranscriptionData}.
         */
        TranscriptionData create(TranscriptionDataConverter internalResponse);
    }

    /**
    * The method called from {@link TranscriptionData} to set it's accessor.
    *
    * @param accessor The accessor.
    */
    public static void setAccessor(final TranscriptionDataContructorProxyAccessor accessor) {
        TranscriptionDataContructorProxy.accessor = accessor;
    }

    /**
    * Creates a new instance of {@link TranscriptionData} backed by an internal instance of
    * {@link TranscriptionDataConverter}.
    *
    * @param internalResponse The internal response.
    * @return A new instance of {@link TranscriptionData}.
    */
    public static TranscriptionData create(TranscriptionDataConverter internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses AudioData which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(TranscriptionData.class.getName(), true,
                    TranscriptionDataContructorProxyAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
