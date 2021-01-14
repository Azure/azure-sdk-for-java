// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class CommunicationIdentityAsyncTests extends CommunicationIdentityClientTestBase {
    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Test
    public void sampleTest() {
        assertFalse(false);
    }

    private CommunicationIdentityAsyncClient setupAsyncClient(CommunicationIdentityClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
