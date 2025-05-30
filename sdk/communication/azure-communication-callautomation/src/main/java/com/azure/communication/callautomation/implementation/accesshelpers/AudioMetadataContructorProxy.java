// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.converters.AudioMetadataConverter;
import com.azure.communication.callautomation.models.AudioMetadata;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper class to access private values of {@link AudioMetaData} across package boundaries.
 */
public final class AudioMetadataContructorProxy {
    private static final ClientLogger LOGGER = new ClientLogger(AudioMetadataContructorProxy.class);
    private static AudioMetadataContructorProxyAccessor accessor;

    private AudioMetadataContructorProxy() {
    }

    /**
    * Type defining the methods to set the non-public properties of a {@link AudioMetadataContructorProxyAccessor}
    * instance.
    */
    public interface AudioMetadataContructorProxyAccessor {
        /**
         * Creates a new instance of {@link AudioMetadata} backed by an internal instance of
         * {@link AudioMetadataConverter}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link AudioMetaData}.
         */
        AudioMetadata create(AudioMetadataConverter internalResponse);
    }

    /**
    * The method called from {@link AudioMetadata} to set it's accessor.
    *
    * @param accessor The accessor.
    */
    public static void setAccessor(final AudioMetadataContructorProxyAccessor accessor) {
        AudioMetadataContructorProxy.accessor = accessor;
    }

    /**
    * Creates a new instance of {@link AudioMetadata} backed by an internal instance of
    * {@link AudioMetadataConverter}.
    *
    * @param internalResponse The internal response.
    * @return A new instance of {@link AudioMetadata}.
    */
    public static AudioMetadata create(AudioMetadataConverter internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses AudioMetadata which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(AudioMetadata.class.getName(), true,
                    AudioMetadataContructorProxyAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
