// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.EmailNotificationHook;
import com.azure.ai.metricsadvisor.administration.models.NotificationHook;
import com.azure.ai.metricsadvisor.administration.models.WebNotificationHook;
import com.azure.core.http.HttpHeaders;

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
        List<String> getAdminsRaw(NotificationHook hook);
        List<String> getEmailsToAlertRaw(EmailNotificationHook emailHook);
        HttpHeaders getHttpHeadersRaw(WebNotificationHook webHook);
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

    public static List<String> getAdminsRaw(NotificationHook hook) {
        return accessor.getAdminsRaw(hook);
    }

    public static List<String> getEmailsToAlertRaw(EmailNotificationHook emailHook) {
        return accessor.getEmailsToAlertRaw(emailHook);
    }

    public static HttpHeaders getHttpHeadersRaw(WebNotificationHook webHook) {
        return accessor.getHttpHeadersRaw(webHook);
    }
}
