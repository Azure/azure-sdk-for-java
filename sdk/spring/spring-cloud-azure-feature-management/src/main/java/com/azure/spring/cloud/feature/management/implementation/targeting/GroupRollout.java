// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.targeting;

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
     * @return the updated GroupRollout object
     */
    public GroupRollout setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the rolloutPercentage
     */
    public double getRolloutPercentage() {
        return rolloutPercentage;
    }

    /**
     * @param rolloutPercentage the rolloutPercentage to set
     * @return the updated GroupRollout object
     */
    public GroupRollout setRolloutPercentage(double rolloutPercentage) {
        this.rolloutPercentage = rolloutPercentage;
        return this;
    }

}
