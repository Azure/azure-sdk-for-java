// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.Hook;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link Hook} instance.
 */
public final class HookHelper {
    private static HookAccessor accessor;

    private HookHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link Hook} instance.
     */
    public interface HookAccessor {
        void setId(Hook hook, String id);
        void setAdmins(Hook hook, List<String> admins);
    }

    /**
     * The method called from {@link Hook} to set it's accessor.
     *
     * @param hookAccessor The accessor.
     */
    public static void setAccessor(final HookAccessor hookAccessor) {
        accessor = hookAccessor;
    }

    public static void setId(Hook hook, String id) {
        accessor.setId(hook, id);
    }

    static void setAdmins(Hook hook, List<String> admins) {
        accessor.setAdmins(hook, admins);
    }
}
