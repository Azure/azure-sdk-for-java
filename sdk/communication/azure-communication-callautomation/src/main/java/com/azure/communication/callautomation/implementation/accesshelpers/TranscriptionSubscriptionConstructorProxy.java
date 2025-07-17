// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.implementation.models.TranscriptionSubscriptionInternal;
import com.azure.communication.callautomation.models.TranscriptionSubscription;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper class to access private values of {@link TranscriptionSubscriptionInternal} across package boundaries.
 */
public final class TranscriptionSubscriptionConstructorProxy {
    private static final ClientLogger LOGGER = new ClientLogger(TranscriptionSubscriptionConstructorProxy.class);
    private static TranscriptionSubscriptionConstructorAccessor accessor;

    private TranscriptionSubscriptionConstructorProxy() {
    }

    /**
    * Type defining the methods to set the non-public properties of a {@link TranscriptionSubscriptionConstructorAccessor}
    * instance.
    */
    public interface TranscriptionSubscriptionConstructorAccessor {
        /**
         * Creates a new instance of {@link TranscriptionSubscription} backed by an internal instance of
         * {@link TranscriptionSubscriptionInternal}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link TranscriptionSubscription}.
         */
        TranscriptionSubscription create(TranscriptionSubscriptionInternal internalResponse);
    }

    /**
    * The method called from {@link TranscriptionSubscription} to set it's accessor.
    *
    * @param accessor The accessor.
    */
    public static void setAccessor(final TranscriptionSubscriptionConstructorAccessor accessor) {
        TranscriptionSubscriptionConstructorProxy.accessor = accessor;
    }

    /**
    * Creates a new instance of {@link TranscriptionSubscription} backed by an internal instance of
    * {@link TranscriptionSubscriptionInternal}.
    *
    * @param internalResponse The internal response.
    * @return A new instance of {@link TranscriptionSubscription}.
    */
    public static TranscriptionSubscription create(TranscriptionSubscriptionInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses TranscriptionSubscription which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName(TranscriptionSubscription.class.getName(), true,
                    TranscriptionSubscriptionConstructorAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
