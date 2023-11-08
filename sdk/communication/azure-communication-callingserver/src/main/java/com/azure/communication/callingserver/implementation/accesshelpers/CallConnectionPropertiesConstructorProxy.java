// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.accesshelpers;

import com.azure.communication.callingserver.implementation.models.CallConnectionPropertiesInternal;
import com.azure.communication.callingserver.models.CallConnectionProperties;

import java.net.URISyntaxException;

/**
 * Helper class to access private values of {@link CallConnectionProperties} across package boundaries.
 */
public final class CallConnectionPropertiesConstructorProxy {
    private static CallConnectionPropertiesConstructorAccessor accessor;

    private CallConnectionPropertiesConstructorProxy() { }

    /**
     * Type defining the methods to set the non-public properties of a {@link CallConnectionPropertiesConstructorAccessor}
     * instance.
     */
    public interface CallConnectionPropertiesConstructorAccessor {
        /**
         * Creates a new instance of {@link CallConnectionProperties} backed by an internal instance of
         * {@link CallConnectionProperties}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link CallConnectionProperties}.
         */
        CallConnectionProperties create(CallConnectionPropertiesInternal internalResponse) throws URISyntaxException;
    }

    /**
     * The method called from {@link CallConnectionProperties} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final CallConnectionPropertiesConstructorAccessor accessor) {
        CallConnectionPropertiesConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link CallConnectionProperties} backed by an internal instance of
     * {@link CallConnectionPropertiesInternal}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link CallConnectionProperties}.
     */
    public static CallConnectionProperties create(CallConnectionPropertiesInternal internalResponse) throws URISyntaxException {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new CallConnectionProperties();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
