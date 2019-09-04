package com.azure.storage.common

import com.azure.core.http.ProxyOptions
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.configuration.ConfigurationManager
import spock.lang.Specification

import java.nio.ByteBuffer
import java.time.OffsetDateTime

class TestBase extends Specification {
    static def AZURE_TEST_MODE = "AZURE_TEST_MODE"

    protected static TestMode testMode = getTestMode()
    private InterceptorManager interceptorManager
    private TestResourceNamer resourceNamer
    private String testName

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        int iterationIndex = fullTestName.lastIndexOf("[")
        int substringIndex = (int) Math.min((iterationIndex != -1) ? iterationIndex : fullTestName.length(), 50)
        this.testName = fullTestName.substring(0, substringIndex)
        this.interceptorManager = new InterceptorManager(className + fullTestName, testMode)
        this.resourceNamer = new TestResourceNamer(className + testName, testMode, interceptorManager.getRecordedData())
    }

    def cleanup() {
        interceptorManager.close()
    }

    static TestMode getTestMode() {
        String testMode = ConfigurationManager.getConfiguration().get(AZURE_TEST_MODE)

        if (testMode != null) {
            try {
                return TestMode.valueOf(testMode.toUpperCase(Locale.US))
            } catch (IllegalArgumentException ignore) {
                return TestMode.PLAYBACK
            }
        }

        return TestMode.PLAYBACK
    }

    static boolean liveMode() {
        return getTestMode() == TestMode.RECORD
    }

    String generateResourceName(String prefix, int length) {
        return resourceNamer.randomName(prefix, length)
    }

    String generateRandomUUID() {
        return resourceNamer.randomUuid()
    }

    OffsetDateTime generateUTCNow() {
        return resourceNamer.now()
    }

    byte[] generateRandomByteArray(int size) {
        long seed = UUID.fromString(resourceNamer.randomUuid()).getMostSignificantBits() & Long.MAX_VALUE
        Random rand = new Random(seed)
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return data
    }

    ByteBuffer generateRandomByteBuffer(int size) {
        return ByteBuffer.wrap(generateRandomByteArray(size))
    }

    // Don't sleep in playback, there is no need to wait for the service to update
    def sleepIfRecord(long milliseconds) {
        if (testMode == TestMode.PLAYBACK) {
            return
        }

        sleep(milliseconds)
    }

    def <T extends BaseClientBuilder> T setupBuilder(BaseClientBuilder<T> builder, String endpoint,
        HttpPipelinePolicy... policies) {
        builder.endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(getRecordPolicy())
        }

        return (T) builder
    }

    def <T extends BaseClientBuilder> T setupBuilder(BaseClientBuilder<T> builder, String connectionString) {
        builder.connectionString(connectionString)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(getRecordPolicy())
        }

        return (T) builder
    }

    def getHttpClient() {
        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()

        if (testMode == TestMode.RECORD) {
            builder.setWiretap(true)

            if (Boolean.parseBoolean(ConfigurationManager.getConfiguration().get("AZURE_TEST_DEBUGGING"))) {
                builder.setProxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            }

            return builder.build()
        } else {
            return interceptorManager.getPlaybackClient()
        }
    }

    def getTestName() {
        return testName
    }

    def getRecordPolicy() {
        return interceptorManager.getRecordPolicy()
    }
}
