/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Class containing Routing in production experiments.
 */
public class Experiments {
    /**
     * List of {Microsoft.Web.Hosting.Administration.RampUpRule} objects.
     */
    private List<RampUpRule> rampUpRules;

    /**
     * Get the rampUpRules value.
     *
     * @return the rampUpRules value
     */
    public List<RampUpRule> rampUpRules() {
        return this.rampUpRules;
    }

    /**
     * Set the rampUpRules value.
     *
     * @param rampUpRules the rampUpRules value to set
     * @return the Experiments object itself.
     */
    public Experiments withRampUpRules(List<RampUpRule> rampUpRules) {
        this.rampUpRules = rampUpRules;
        return this;
    }

}
