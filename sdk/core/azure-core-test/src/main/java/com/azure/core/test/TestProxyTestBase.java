// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for running live and playback tests using test-proxy
 */
@ExtendWith(TestProxyExtensions.class)
public abstract class TestProxyTestBase extends TestBase {
    static {
        setTestProxyEnabled();
    }

    /**
     * Creates an instance of {@link TestProxyTestBase}.
     */
    public TestProxyTestBase() {
        super();
    }
}
