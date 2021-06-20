// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core;

import org.junit.Assert;
import org.junit.Test;

import static com.azure.spring.core.ApplicationId.AZURE_SPRING_STORAGE_QUEUE;
import static com.azure.spring.cloud.context.core.util.Constants.VERSION;


public class ApplicationIdTest {

    @Test
    public void maxLength() {
        Assert.assertTrue((AZURE_SPRING_STORAGE_QUEUE + VERSION).length() <= 24);
    }

}
