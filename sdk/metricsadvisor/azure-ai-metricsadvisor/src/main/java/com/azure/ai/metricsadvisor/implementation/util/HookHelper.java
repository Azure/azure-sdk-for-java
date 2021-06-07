// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.NotificationHook;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link NotificationHook} instance.
 */
public final class HookHelper {
    private static HookAccessor accessor;

    private HookHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link NotificationHook} instance.
     */
    public interface HookAccessor {
        void setId(NotificationHook hook, String id);
        void setAdminEmails(NotificationHook hook, List<String> admins);
    }

    /**
     * The method called from {@link NotificationHook} to set it's accessor.
     *
     * @param hookAccessor The accessor.
     */
    public static void setAccessor(final HookAccessor hookAccessor) {
        accessor = hookAccessor;
    }

    public static void setId(NotificationHook hook, String id) {
        accessor.setId(hook, id);
    }

    static void setAdminEmails(NotificationHook hook, List<String> adminEmails) {
        accessor.setAdminEmails(hook, adminEmails);
    }
}
