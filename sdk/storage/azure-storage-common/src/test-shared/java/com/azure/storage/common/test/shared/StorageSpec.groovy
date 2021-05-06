// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared

import com.azure.core.http.HttpClient
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import spock.lang.Specification

class StorageSpec extends Specification {
    private static final TestEnvironment ENVIRONMENT = new TestEnvironment();

    private InterceptorManager interceptorManager
    private StorageResourceNamer namer

    def setup() {
        def testName = getTestName()
        interceptorManager = new InterceptorManager(testName, ENVIRONMENT.testMode)
        namer = new StorageResourceNamer(testName, ENVIRONMENT.testMode, interceptorManager.getRecordedData())
        System.out.printf("========================= %s =========================%n", testName)
    }

    def cleanup() {
        interceptorManager.close()
    }

    protected static TestEnvironment getEnv() {
        return ENVIRONMENT
    }

    protected StorageResourceNamer getNamer() {
        Objects.requireNonNull(namer, "namer has not been initialized yet")
        return namer
    }

    protected HttpPipelinePolicy getRecordPolicy() {
        if (ENVIRONMENT.testMode == TestMode.RECORD) {
            return interceptorManager.getRecordPolicy()
        } else {
            return { context, next -> return next.process() }
        }
    }

    protected HttpClient getHttpClient() {
        if (ENVIRONMENT.testMode != TestMode.PLAYBACK) {
            NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()
            return builder.build()
        } else {
            return interceptorManager.getPlaybackClient()
        }
    }

    private String getTestName() {
        def iterationInfo = specificationContext.currentIteration
        def featureInfo = iterationInfo.getParent()
        def specInfo = featureInfo.getParent()
        def fullName = specInfo.getName() + featureInfo.getName().split(" ").collect { it.capitalize() }.join("")

        if (iterationInfo.getDataValues().length == 0) {
            return fullName
        }
        def prefix = fullName
        def suffix = "[" + iterationInfo.getIterationIndex() + "]"

        return prefix + suffix
    }
}
