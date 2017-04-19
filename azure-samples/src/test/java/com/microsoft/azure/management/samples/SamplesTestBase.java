/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.rest.RestClient;

public class SamplesTestBase extends TestBase {
    protected Azure azure;
    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        azure = Azure
                .authenticate(restClient, defaultSubscription, domain).withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {
    }
}
