// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core;

import org.junit.Assert;
import org.junit.Test;

import static com.azure.spring.cloud.context.core.util.Constants.SPRING_INTEGRATION_STORAGE_QUEUE_APPLICATION_ID;


public class ApplicationIdTest {

    @Test
    public void maxLength() {
        Assert.assertTrue(SPRING_INTEGRATION_STORAGE_QUEUE_APPLICATION_ID.length() <= 24);
    }

}
