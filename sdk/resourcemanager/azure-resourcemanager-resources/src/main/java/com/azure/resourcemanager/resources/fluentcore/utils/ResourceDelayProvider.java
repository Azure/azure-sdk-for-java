// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.management.provider.DelayProvider;

import java.time.Duration;

/**
 * The ResourceDelayProvider to help thread sleep.
 */
public class ResourceDelayProvider implements DelayProvider {
    @Override
    public Duration getDelayDuration(Duration delay) {
        return delay;
    }
}
