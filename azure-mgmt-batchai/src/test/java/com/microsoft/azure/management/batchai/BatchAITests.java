/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batchai;

import com.microsoft.azure.Page;
import com.microsoft.azure.management.batchai.implementation.BatchAIManager;
import com.microsoft.azure.management.batchai.implementation.OperationInner;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponse;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;


public class BatchAITests extends TestBase {
    private BatchAIManager batchAIManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) throws IOException {
        batchAIManager = BatchAIManager.authenticate(restClient, defaultSubscription);
    }

    @Test
    public void simpleTest() {
        ServiceResponse<Page<OperationInner>> page = batchAIManager.inner()
                .operations()
                .listSinglePageAsync()
                .toBlocking().last();

        assertNotNull(page);
    }

    @Override
    protected void cleanUpResources() {

    }
}
