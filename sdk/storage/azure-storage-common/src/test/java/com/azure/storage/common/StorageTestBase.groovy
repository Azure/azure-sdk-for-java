package com.azure.storage.common

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.Configuration
import com.azure.core.util.FluxUtil
import com.azure.core.util.logging.ClientLogger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Requires
import spock.lang.Specification

import java.nio.ByteBuffer
import java.time.OffsetDateTime

class StorageTestBase extends Specification {
    private ClientLogger logger = new ClientLogger(StorageTestBase.class)

    private static String AZURE_TEST_DEBUGGING = "AZURE_TEST_DEBUGGING"
    private static String AZURE_TEST_MODE = "AZURE_TEST_MODE"
    protected static TestMode testMode = setupTestMode()

    protected InterceptorManager interceptorManager
    protected boolean recordLiveMode
    protected TestResourceNamer resourceNamer
    protected String testName

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        int iterationIndex = fullTestName.lastIndexOf('[')
        int substringIndex = (int) Math.min((iterationIndex != -1) ? iterationIndex : fullTestName.length(), 50)
        this.testName = fullTestName.substring(0, substringIndex)
        this.interceptorManager = new InterceptorManager(className + fullTestName, testMode)
        this.resourceNamer = new TestResourceNamer(className + testName, testMode, interceptorManager.getRecordedData())

        // If the test doesn't have the Requires tag record it in live mode.
        recordLiveMode = specificationContext.getCurrentIteration().getDescription().getAnnotation(Requires.class) == null
    }

    private static TestMode setupTestMode() {
        String testMode = Configuration.getGlobalConfiguration().get(AZURE_TEST_MODE)

        if (testMode != null) {
            try {
                return TestMode.valueOf(testMode.toUpperCase(Locale.US))
            } catch (IllegalArgumentException ignore) {
                return TestMode.PLAYBACK
            }
        }

        return TestMode.PLAYBACK
    }

    protected static boolean isLiveMode() {
        return testMode != TestMode.PLAYBACK
    }

    protected StorageSharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (testMode == TestMode.PLAYBACK) {
            accountName = "azstoragesdkaccount"
            accountKey = "astorageaccountkey"
        } else {
            accountName = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_NAME")
            accountKey = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_KEY")
        }

        if (accountName == null || accountKey == null) {
            logger.warning("Account name or key for the {} account was null. Test's requiring these credentials will fail.", accountType)
            return null
        }

        return new StorageSharedKeyCredential(accountName, accountKey)
    }

    protected HttpClient getHttpClient() {
        if (testMode == TestMode.PLAYBACK) {
            return interceptorManager.getPlaybackClient()
        } else {
            def builder = new NettyAsyncHttpClientBuilder()

            if (testMode == TestMode.RECORD) {
                builder.wiretap(true)
            }

            if (Boolean.parseBoolean(Configuration.getGlobalConfiguration().get(AZURE_TEST_DEBUGGING))) {
                builder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            }

            return builder.build()
        }
    }

    protected String generateResourceName(String prefix, int maxLength) {
        return resourceNamer.randomName(prefix, maxLength)
    }

    protected String getConfigValue(String value) {
        return resourceNamer.recordValueFromConfig(value)
    }

    protected String getRandomUUID() {
        return resourceNamer.randomUuid()
    }

    protected OffsetDateTime getUtcNow() {
        return resourceNamer.now()
    }

    /*
     * Limited to an int because ByteBuffer sizes can only be an int, long is not supported.
     */
    protected ByteBuffer getRandomData(int size) {
        return ByteBuffer.wrap(getRandomByteArray(size))
    }

    /*
     * Limited to an int because array sizes can only be an int, long is not supported.
     */
    protected byte[] getRandomByteArray(int size) {
        def seed = UUID.fromString(getRandomUUID()).getMostSignificantBits() & Long.MAX_VALUE
        def rand = new Random(seed)
        def data = new byte[size]
        rand.nextBytes(data)
        return data
    }

    /*
     * We only allow int because anything larger than 2GB is left to stress and performance tests.
     */
    protected File getRandomFile(int size) {
        def file = File.createTempFile(getRandomUUID(), ".txt")
        file.deleteOnExit()
        def outputStream = new FileOutputStream(file)
        outputStream.write(getRandomByteArray(size))
        outputStream.close()
        return file
    }

    protected static Mono<ByteBuffer> collectBytesInBuffer(Flux<ByteBuffer> stream) {
        return FluxUtil.collectBytesInByteBufferStream(stream).flatMap({ Mono.just(ByteBuffer.wrap(it)) })
    }

    protected static void sleepIfLive(long milliseconds) {
        if (testMode == TestMode.PLAYBACK) {
            return
        }

        sleep(milliseconds)
    }
}
