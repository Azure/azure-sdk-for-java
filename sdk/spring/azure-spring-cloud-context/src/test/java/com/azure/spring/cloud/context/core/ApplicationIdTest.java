// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.context.core.util.Constants.SPRING_INTEGRATION_STORAGE_QUEUE_APPLICATION_ID;


public class ApplicationIdTest {

    @Test
    public void maxLength() {
        Assertions.assertTrue(SPRING_INTEGRATION_STORAGE_QUEUE_APPLICATION_ID.length() <= 24);
    }

}
