// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.targeting;

/**
 * The settings that are used to configure the TargetingFilter feature filter.
 */
public class TargetingFilterSettings {

    private Audience audience;

    /**
     * @return the audience
     */
    public Audience getAudience() {
        return audience;
    }

    /**
     * @param audience the audience to set
     */
    public void setAudience(Audience audience) {
        this.audience = audience;
    }

}
