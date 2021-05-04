// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.util.Configuration
import spock.lang.Specification

class StorageSpec extends Specification {
    protected static final TestEnvironment ENVIRONMENT = new TestEnvironment();

    private String testName
    private InterceptorManager interceptorManager
    private StorageResourceNamer resourceNamer

    def setup() {
        testName = getTestName()
        interceptorManager = new InterceptorManager(testName, ENVIRONMENT.testMode)
        resourceNamer = new StorageResourceNamer(testName, ENVIRONMENT.testMode, interceptorManager.getRecordedData())
        System.out.printf("========================= %s =========================%n", testName)
    }

    def cleanup() {
        interceptorManager.close()
    }

    protected StorageResourceNamer getResourceNamer() {
        Objects.requireNonNull(resourceNamer, "resourceNamer has not been initialized yet")
        return resourceNamer
    }

    protected HttpPipelinePolicy getRecordPolicy() {
        if (ENVIRONMENT.testMode == TestMode.RECORD) {
            return interceptorManager.getRecordPolicy()
        } else {
            return { context, next -> return next.process() }
        }
    }

    protected HttpClient getHttpClient() {
        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()
        if (ENVIRONMENT.testMode != TestMode.PLAYBACK) {
            builder.wiretap(true)

            if (Boolean.parseBoolean(Configuration.getGlobalConfiguration().get("AZURE_TEST_DEBUGGING"))) {
                builder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            }

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
