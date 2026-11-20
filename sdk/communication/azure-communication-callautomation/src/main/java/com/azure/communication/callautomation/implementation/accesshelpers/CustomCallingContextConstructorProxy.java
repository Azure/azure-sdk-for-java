// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.accesshelpers;

import com.azure.communication.callautomation.models.CustomCallingContext;

import java.util.Map;

/**
 * Helper class to access private values of {@link CustomCallingContext} across package boundaries.
 */
public final class CustomCallingContextConstructorProxy {
    private static CustomCallingContextConstructorAccessor accessor;

    private CustomCallingContextConstructorProxy() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link CustomCallingContextConstructorAccessor}
     * instance.
     */
    public interface CustomCallingContextConstructorAccessor {
        /**
         * Creates a new instance of {@link CustomCallingContext} backed by the provided SIP and VOIP headers.
         *
         * @param sipHeaders The SIP headers.
         * @param voipHeaders The VOIP headers.
         * @return A new instance of {@link CustomCallingContext}.
         */
        CustomCallingContext create(Map<String, String> sipHeaders, Map<String, String> voipHeaders);
    }

    /**
     * The method called from {@link CustomCallingContext} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final CustomCallingContextConstructorAccessor accessor) {
        CustomCallingContextConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link CustomCallingContext} backed by the provided SIP and VOIP headers.
     *
     * @param sipHeaders The SIP headers.
     * @param voipHeaders The VOIP headers.
     * @return A new instance of {@link CustomCallingContext}.
     */
    public static CustomCallingContext create(Map<String, String> sipHeaders, Map<String, String> voipHeaders) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses CustomCallingContext which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            try {
                Class.forName("com.azure.communication.callautomation.models.CustomCallingContext", true,
                    CustomCallingContextConstructorProxy.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        assert accessor != null;
        return accessor.create(sipHeaders, voipHeaders);
    }
}
