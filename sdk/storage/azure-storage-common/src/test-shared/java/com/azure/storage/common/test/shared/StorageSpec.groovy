// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared

import com.azure.core.credential.TokenRequestContext
import com.azure.core.http.HttpClient
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.util.ServiceVersion
import com.azure.core.util.logging.ClientLogger
import com.azure.identity.EnvironmentCredentialBuilder
import okhttp3.ConnectionPool
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.function.Predicate
import java.util.function.Supplier

class StorageSpec extends Specification {
    private static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance()
    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientBuilder().build()
    private static final HttpClient OK_HTTP_CLIENT = new OkHttpAsyncHttpClientBuilder().connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES)).build()
    private static final ClientLogger LOGGER = new ClientLogger(StorageSpec.class)

    static {
        // Dump threads if run goes over 30 minutes and there's a possible deadlock.
        ThreadDumper.initialize()
    }

    private InterceptorManager interceptorManager
    private StorageResourceNamer namer

    def setup() {
        def testName = TestNameProvider.getTestName(specificationContext.getCurrentIteration());
        interceptorManager = new InterceptorManager(testName, ENVIRONMENT.testMode)
        namer = new StorageResourceNamer(testName, ENVIRONMENT.testMode, interceptorManager.getRecordedData())
        LOGGER.info("Test {} will use {} resource prefix.", testName, namer.resourcePrefix)
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

    protected getData() {
        return TestDataFactory.getInstance();
    }

    protected <T> T instrument(T builder) {
        // Groovy style reflection. All our builders follow this pattern.
        builder."httpClient"(getHttpClient())
        if (ENVIRONMENT.testMode == TestMode.RECORD) {
            builder."addPolicy"(interceptorManager.getRecordPolicy())
        }

        if (ENVIRONMENT.serviceVersion != null) {
            Class<ServiceVersion> serviceVersionClass = builder.class.methods
                .find { it.name == "serviceVersion" && it.parameterCount == 1}.parameterTypes[0] as Class<ServiceVersion>
            def parsedServiceVersion = Enum.valueOf(serviceVersionClass, ENVIRONMENT.serviceVersion)
            builder."serviceVersion"(parsedServiceVersion)
            builder."addPolicy"(new ServiceVersionValidationPolicy(parsedServiceVersion.version))
        }

        HttpLogOptions httpLogOptions = builder."getDefaultHttpLogOptions"()
        httpLogOptions.setLogLevel(HttpLogDetailLevel.HEADERS)
        builder."httpLogOptions"(httpLogOptions)

        return builder
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
            switch (ENVIRONMENT.httpClientType) {
                case TestHttpClientType.NETTY:
                    return NETTY_HTTP_CLIENT
                case TestHttpClientType.OK_HTTP:
                    return OK_HTTP_CLIENT
                default:
                    throw new IllegalArgumentException("Unknown http client type: " + ENVIRONMENT.httpClientType)
            }
        } else {
            return interceptorManager.getPlaybackClient()
        }
    }

    private static String getAuthToken() {
        if (env.testMode == TestMode.PLAYBACK) {
            // we just need some string to satisfy SDK for playback mode. Recording framework handles this fine.
            return "recordingBearerToken"
        }
        return new EnvironmentCredentialBuilder().build()
            .getToken(new TokenRequestContext().setScopes(["https://storage.azure.com/.default"]))
            .map { it.getToken() }
            .block()
    }

    protected <T, E extends Exception> T retry(
        Supplier<T> action, Predicate<E> retryPredicate,
        int times=6, Duration delay=Duration.ofSeconds(10)) {
        for (i in 0..<times) {
            try {
                return action.get()
            } catch (Exception e) {
                if (!retryPredicate(e)) {
                    throw e
                } else {
                    Thread.sleep(delay.toMillis())
                }
            }
        }
    }
}
