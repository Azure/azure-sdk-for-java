/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;

import com.azure.management.Azure;
import com.azure.management.RestClient;
import com.azure.management.resources.core.TestBase;

public class SamplesTestBase extends TestBase {
    protected Azure azure;

    public SamplesTestBase() {
        super(RunCondition.BOTH);
    }

    public SamplesTestBase(RunCondition runCondition) {
        super(runCondition);
    }

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        azure = Azure
                .authenticate(restClient, domain, defaultSubscription)
                .withSdkContext(sdkContext)
                .withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {
    }
}
