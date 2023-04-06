/*
 * // Copyright (c) Microsoft Corporation. All rights reserved.
 * // Licensed under the MIT License.
 */

package com.azure.core.test;

import com.azure.core.test.utils.TestProxyManager;
import com.azure.core.test.utils.TestUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

public class TestProxyExtensions implements BeforeAllCallback, AfterAllCallback {
    @Override
    public void afterAll(ExtensionContext context) {
        TestMode testMode = TestBase.initializeTestMode();
        if(testMode == TestMode.RECORD || testMode == TestMode.PLAYBACK) {
            TestProxyManager testProxyManager = getStore(context).remove("proxymanager", TestProxyManager.class);
            testProxyManager.stopProxy();
        }

    }

    @Override
    public void beforeAll(ExtensionContext context) {
        TestMode testMode = TestBase.initializeTestMode();
        if(testMode == TestMode.RECORD || testMode == TestMode.PLAYBACK) {
            TestProxyManager manager = new TestProxyManager(TestUtils.getRecordFolder());
            manager.startProxy();
            getStore(context).put("proxymanager", manager);
        }
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context.getRequiredTestClass()));
    }
}
