// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

public class TopicSessionTests extends SessionTests {
    @Override
    public String getEntityNamePrefix() {
        return "TopicSessionTests";
    }

    @Override
    public boolean isEntityQueue() {
        return false;
    }

    @Override
    public boolean isEntityPartitioned() {
        return false;
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }
}
