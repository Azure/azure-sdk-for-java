// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.storage.fluent.models.UsageInner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class UsageOperationsTests extends StorageManagementTest {
    @Test
    @Disabled("Service is no longer supporting listing")
    public void canGetUsages() throws Exception {
        PagedIterable<UsageInner> usages = storageManager.usages().list();
        System.out.println(TestUtilities.getSize(usages));
    }

    @Override
    protected void cleanUpResources() {
    }
}
