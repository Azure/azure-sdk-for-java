// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.HookHelper;

import java.util.List;

/**
 * Describes a hook that receives anomaly incident alerts.
 */
public abstract class NotificationHook {
    private String id;
    private List<String> adminEmails;

    static {
        HookHelper.setAccessor(new HookHelper.HookAccessor() {
            @Override
            public void setId(NotificationHook hook, String id) {
                hook.setId(id);
            }

            @Override
            public void setAdminEmails(NotificationHook hook, List<String> adminEmails) {
                hook.setAdminEmails(adminEmails);
            }
        });
    }

    /**
     * Gets the id of the NotificationHook.
     *
     * @return The id of the NotificationHook.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the name for the Notification hook.
     *
     * @return The name.
     */
    public abstract String getName();

    /**
     * Gets the description for the Notification hook.
     *
     * @return The description.
     */
    public abstract String getDescription();

    /**
     * The list of admin emails for the Notification hook.
     *
     * @return The emails of admins.
     */
    public List<String> getAdminEmails() {
        return this.adminEmails;
    }

    void setId(String id) {
        this.id = id;
    }

    void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }
}
