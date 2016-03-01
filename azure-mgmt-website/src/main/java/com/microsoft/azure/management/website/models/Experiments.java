/**
 * Object]
 */

package com.microsoft.azure.management.website.models;

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
    public List<RampUpRule> getRampUpRules() {
        return this.rampUpRules;
    }

    /**
     * Set the rampUpRules value.
     *
     * @param rampUpRules the rampUpRules value to set
     */
    public void setRampUpRules(List<RampUpRule> rampUpRules) {
        this.rampUpRules = rampUpRules;
    }

}
