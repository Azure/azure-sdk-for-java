// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

/**
 * Base class for running live and playback tests using test-proxy
 */
public class TestProxyTestBase extends TestBase {
    static {
        enableTestProxy = true;
    }
}
