// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.implementation.targeting;

/**
 * Properties for defining a rollout for a given group.
 */
public class GroupRollout {

    private String name;

    private double rolloutPercentage;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the rolloutPercentage
     */
    public double getRolloutPercentage() {
        return rolloutPercentage;
    }

    /**
     * @param rolloutPercentage the rolloutPercentage to set
     */
    public void setRolloutPercentage(double rolloutPercentage) {
        this.rolloutPercentage = rolloutPercentage;
    }

}
