// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.converters.TranscriptionMetadataConverter;
import com.azure.communication.callautomation.models.TranscriptionMetadata;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper class to access private values of {@link TranscriptionMetadata} across package boundaries.
 */
public final class TranscriptionMetadataContructorProxy {
    private static final ClientLogger LOGGER = new ClientLogger(TranscriptionMetadataContructorProxy.class);
    private static TranscriptionMetadataContructorProxyAccessor accessor;

    private TranscriptionMetadataContructorProxy() {
    }

    /**
    * Type defining the methods to set the non-public properties of a {@link TranscriptionMetadataContructorProxyAccessor}
    * instance.
    */
    public interface TranscriptionMetadataContructorProxyAccessor {
        /**
         * Creates a new instance of {@link TranscriptionMetadata} backed by an internal instance of
         * {@link TranscriptionMetadataConverter}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link TranscriptionMetadata}.
         */
        TranscriptionMetadata create(TranscriptionMetadataConverter internalResponse);
    }

    /**
    * The method called from {@link TranscriptionMetadata} to set it's accessor.
    *
    * @param accessor The accessor.
    */
    public static void setAccessor(final TranscriptionMetadataContructorProxyAccessor accessor) {
        TranscriptionMetadataContructorProxy.accessor = accessor;
    }

    /**
    * Creates a new instance of {@link TranscriptionMetadata} backed by an internal instance of
    * {@link TranscriptionMetadataConverter}.
    *
    * @param internalResponse The internal response.
    * @return A new instance of {@link TranscriptionMetadata}.
    */
    public static TranscriptionMetadata create(TranscriptionMetadataConverter internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses TranscriptionMetadata which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(TranscriptionMetadata.class.getName(), true,
                    TranscriptionMetadataContructorProxyAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
