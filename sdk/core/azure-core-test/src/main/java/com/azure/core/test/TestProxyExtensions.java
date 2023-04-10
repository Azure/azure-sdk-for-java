// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.utils.TestProxyManager;
import com.azure.core.test.utils.TestUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.net.URL;

/**
 * Extensions to manage creation of TestProxyManager instances. These instances are created per test class, as that's
 * the granularity we are offered from JUnit.
 */
public class TestProxyExtensions implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
    @Override
    public void afterAll(ExtensionContext context) {
        TestMode testMode = TestBase.initializeTestMode();
        if (testMode == TestMode.RECORD || testMode == TestMode.PLAYBACK) {
            TestProxyManager testProxyManager = getStore(context).remove("proxyManager", TestProxyManager.class);
            testProxyManager.stopProxy();
        }

    }

    @Override
    public void beforeAll(ExtensionContext context) {
        TestMode testMode = TestBase.initializeTestMode();
        if (testMode == TestMode.RECORD || testMode == TestMode.PLAYBACK) {
            TestProxyManager manager = new TestProxyManager(TestUtils.getRecordFolder());
            manager.startProxy();
            getStore(context).put("proxyManager", manager);
            getStore(context).put("proxyUrl", manager.getProxyUrl());
        }
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context.getRequiredTestClass()));
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (!context.getTestInstance().isPresent()) {
            throw new RuntimeException("ExtensionContext does not have an instance.");
        }
        TestBase testBase = (TestBase) context.getTestInstance().get();
        testBase.setProxyUrl(getStore(context).get("proxyUrl", URL.class));
    }
}
