// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;
import com.azure.communication.callautomation.implementation.models.MediaStreamingSubscriptionInternal;
import com.azure.communication.callautomation.models.MediaStreamingSubscription;

/**
 * Helper class to access private values of {@link MediaStreamingSubscriptionInternal} across package boundaries.
 */
public final class MediaStreamingSubscriptionConstructorProxy {
    private static MediaStreamingSubscriptionConstructorAccessor accessor;

    private MediaStreamingSubscriptionConstructorProxy() { }

     /**
     * Type defining the methods to set the non-public properties of a {@link MediaStreamingSubscriptionConstructorAccessor}
     * instance.
     */
    public interface MediaStreamingSubscriptionConstructorAccessor {
        /**
         * Creates a new instance of {@link MediaStreamingSubscription} backed by an internal instance of
         * {@link MediaStreamingSubscriptionInternal}.
         *
         * @param internalResponse The internal response.
         * @return A new instance of {@link MediaStreamingSubscription}.
         */
        MediaStreamingSubscription create(MediaStreamingSubscriptionInternal internalResponse);
    }

     /**
     * The method called from {@link MediaStreamingSubscription} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final MediaStreamingSubscriptionConstructorAccessor accessor) {
        MediaStreamingSubscriptionConstructorProxy.accessor = accessor;
    }

     /**
     * Creates a new instance of {@link MediaStreamingSubscription} backed by an internal instance of
     * {@link MediaStreamingSubscriptionInternal}.
     *
     * @param internalResponse The internal response.
     * @return A new instance of {@link MediaStreamingSubscription}.
     */
    public static MediaStreamingSubscription create(MediaStreamingSubscriptionInternal internalResponse) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses MediaStreamingSubscription which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new MediaStreamingSubscription();
        }

        assert accessor != null;
        return accessor.create(internalResponse);
    }
}
