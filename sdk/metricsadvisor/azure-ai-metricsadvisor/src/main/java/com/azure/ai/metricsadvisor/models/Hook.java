// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.util.List;

/**
 * Describes a hook that receives anomaly incident alerts.
 */
public abstract class Hook {
    /**
     * Gets the id.
     *
     * @return The id.
     */
    public abstract String getId();

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
    public abstract List<String> getAdmins();
}
