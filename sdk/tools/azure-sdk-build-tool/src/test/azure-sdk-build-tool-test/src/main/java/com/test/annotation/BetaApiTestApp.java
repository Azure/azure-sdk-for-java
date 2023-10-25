// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.test.annotation;

import com.azure.cosmos.ChangeFeedProcessorBuilder;

/**
 * Test @Beta annotation usage.
 */
public class BetaApiTestApp {
    public static void main(String[] args) {
        ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder();
        // this is a beta API
        changeFeedProcessorBuilder.handleAllVersionsAndDeletesChanges(null);
    }
}
