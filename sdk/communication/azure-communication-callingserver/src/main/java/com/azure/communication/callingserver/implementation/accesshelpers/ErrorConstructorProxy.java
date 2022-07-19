// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.accesshelpers;

import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.core.exception.HttpResponseException;

/**
 * Helper class to access private values of {@link CallingServerErrorException} across package boundaries.
 */
public final class ErrorConstructorProxy {
    private static ErrorConstructorProxy.ErrorConstructorAccessor accessor;

    private ErrorConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link CallConnectionPropertiesConstructorProxy.CallConnectionPropertiesConstructorAccessor}
     * instance.
     */
    public interface ErrorConstructorAccessor {
        /**
         * Creates a new instance of {@link CallingServerErrorException} backed by an internal instance of
         * {@link CallingServerErrorException}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link CallingServerErrorException}.
         */
        CallingServerErrorException create(HttpResponseException internalResponse);
    }

    /**
     * The method called from {@link CallingServerErrorException} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final ErrorConstructorProxy.ErrorConstructorAccessor accessor) {
        ErrorConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link CallingServerErrorException} backed by an internal instance of
     * {@link HttpResponseException}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link CallingServerErrorException}.
     */
    public static CallingServerErrorException create(HttpResponseException internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new CallingServerErrorException();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
