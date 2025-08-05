// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.converters.AudioDataConverter;
import com.azure.communication.callautomation.models.AudioData;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper class to access private values of {@link AudioData} across package boundaries.
 */
public final class AudioDataContructorProxy {
    private static final ClientLogger LOGGER = new ClientLogger(AudioDataContructorProxy.class);

    private static AudioDataContructorProxyAccessor accessor;

    private AudioDataContructorProxy() {
    }

    /**
    * Type defining the methods to set the non-public properties of a {@link AudioDataContructorProxyAccessor}
    * instance.
    */
    public interface AudioDataContructorProxyAccessor {
        /**
         * Creates a new instance of {@link AudioData} backed by an internal instance of
         * {@link AudioDataConvertor}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link AudioData}.
         */
        AudioData create(AudioDataConverter internalResponse);

        /**
         * Creates a new instance of {@link AudioData}
         *
         * @param data The internal response.
         * @return A new instance of {@link AudioData}.
         */
        AudioData create(BinaryData data);
    }

    /**
    * The method called from {@link AudioData} to set it's accessor.
    *
    * @param accessor The accessor.
    */
    public static void setAccessor(final AudioDataContructorProxyAccessor accessor) {
        AudioDataContructorProxy.accessor = accessor;
    }

    /**
    * Creates a new instance of {@link AudioData} backed by an internal instance of
    * {@link AudioDataConverter}.
    *
    * @param internalResponse The internal response.
    * @return A new instance of {@link AudioData}.
    */
    public static AudioData create(AudioDataConverter internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses AudioData which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(AudioData.class.getName(), true, AudioDataContructorProxyAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }

    /**
     * Creates a new instance of {@link AudioData} 
     *
     * @param data The audio data.
     * @return A new instance of {@link AudioData}.
     */
    public static AudioData create(BinaryData data) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses AudioData which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(AudioData.class.getName(), true, AudioDataContructorProxyAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(data);
    }
}
