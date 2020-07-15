/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager.targeting;

public class GroupRollout {

    private String name;

    private double rolloutPercentage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRolloutPercentage() {
        return rolloutPercentage;
    }

    public void setRolloutPercentage(double rolloutPercentage) {
        this.rolloutPercentage = rolloutPercentage;
    }

}
