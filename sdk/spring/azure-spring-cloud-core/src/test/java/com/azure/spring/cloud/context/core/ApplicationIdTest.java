// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.azure.spring.core.ApplicationId.AZURE_SPRING_INTEGRATION_STORAGE_QUEUE;
import static com.azure.spring.core.ApplicationId.VERSION;


public class ApplicationIdTest {

    @Test
    public void maxLength() {
        Assertions.assertTrue((AZURE_SPRING_INTEGRATION_STORAGE_QUEUE + VERSION).length() <= 24);
    }

}
