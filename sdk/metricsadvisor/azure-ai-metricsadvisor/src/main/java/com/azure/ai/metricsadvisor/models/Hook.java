// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.implementation.util.HookHelper;

import java.util.List;

/**
 * Describes a hook that receives anomaly incident alerts.
 */
public abstract class Hook {
    private String id;
    private List<String> admins;

    static {
        HookHelper.setAccessor(new HookHelper.HookAccessor() {
            @Override
            public void setId(Hook hook, String id) {
                hook.setId(id);
            }

            @Override
            public void setAdmins(Hook hook, List<String> admins) {
                hook.setAdmins(admins);
            }
        });
    }

    /**
     * Gets the id.
     *
     * @return The id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the name for the email hook.
     *
     * @return The name.
     */
    public abstract String getName();

    /**
     * Gets the description for the email hook.
     *
     * @return The description.
     */
    public abstract String getDescription();

    /**
     * The admins for the hook.
     *
     * @return The admins.
     */
    public List<String> getAdmins() {
        return this.admins;
    }

    void setId(String id) {
        this.id = id;
    }

    void setAdmins(List<String> admins) {
        this.admins = admins;
    }
}
