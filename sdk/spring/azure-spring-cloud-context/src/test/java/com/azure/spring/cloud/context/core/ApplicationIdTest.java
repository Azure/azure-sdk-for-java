// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core;

import static com.azure.spring.cloud.context.core.util.Constants.SPRING_CLOUD_STORAGE_BLOB_APPLICATION_ID;
import static com.azure.spring.cloud.context.core.util.Constants.SPRING_CLOUD_STORAGE_FILE_SHARE_APPLICATION_ID;
import static com.azure.spring.cloud.context.core.util.Constants.SPRING_INTEGRATION_STORAGE_QUEUE_APPLICATION_ID;

import org.junit.Assert;
import org.junit.Test;


public class ApplicationIdTest {

    @Test
    public void maxLength() {
        Assert.assertTrue(SPRING_CLOUD_STORAGE_BLOB_APPLICATION_ID.length() <= 24);
        Assert.assertTrue(SPRING_CLOUD_STORAGE_FILE_SHARE_APPLICATION_ID.length() <= 24);
        Assert.assertTrue(SPRING_INTEGRATION_STORAGE_QUEUE_APPLICATION_ID.length() <= 24);
    }

}
