// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class CommunicationIdentityTests extends CommunicationIdentityClientTestBase {
    private CommunicationIdentityClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }
    
    @Test
    public void sampleTest() {
        assertFalse(false);
    }

    private CommunicationIdentityClient setupClient(CommunicationIdentityClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
