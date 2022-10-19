// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.util;

import java.time.Duration;

/**
 * Util class for actuator related constants.
 */
public final class ActuateConstants {

    private ActuateConstants() {

    }

    public static final Duration DEFAULT_HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(3);
}
