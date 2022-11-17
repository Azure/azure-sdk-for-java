// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.common;

import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;


@Timeout(value = 5, unit = TimeUnit.MINUTES)
public abstract class BaseIT {
}
