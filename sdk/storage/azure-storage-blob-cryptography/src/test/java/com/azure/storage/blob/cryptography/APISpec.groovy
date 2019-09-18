package com.azure.storage.blob.cryptography

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.http.rest.Response
import com.azure.core.implementation.util.FluxUtil
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.Configuration
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobProperties
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.specialized.LeaseClientBuilder
import com.azure.storage.common.BaseClientBuilder
import com.azure.storage.common.credentials.SharedKeyCredential
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime

class APISpec extends Specification {

    /*
    The values below are used to create data-driven tests for access conditions.
     */
    static final OffsetDateTime oldDate = OffsetDateTime.now().minusDays(1)

    static final OffsetDateTime newDate = OffsetDateTime.now().plusDays(1)

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedEtag = "received"

    static final String garbageEtag = "garbage"

    String containerPrefix = "jtc" // java test container

    String blobPrefix = "javablob"

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedLeaseID = "received"

    static final String garbageLeaseID = UUID.randomUUID().toString()

    @Shared
    ClientLogger logger = new ClientLogger(APISpec.class)

    static SharedKeyCredential primaryCredential
    static SharedKeyCredential alternateCredential
    static SharedKeyCredential blobCredential
    static SharedKeyCredential premiumCredential
    static TestMode testMode
    private boolean recordLiveMode

    static def AZURE_TEST_MODE = "AZURE_TEST_MODE"
    static def PRIMARY_STORAGE = "PRIMARY_STORAGE_"
    static def SECONDARY_STORAGE = "SECONDARY_STORAGE_"
    static def BLOB_STORAGE = "BLOB_STORAGE_"
    static def PREMIUM_STORAGE = "PREMIUM_STORAGE_"

    private TestResourceNamer resourceNamer
    private InterceptorManager interceptorManager
    protected String testName

    // Fields used for conveniently creating blobs with data.
    static final String defaultText = "default"

    public static final ByteBuffer defaultData = ByteBuffer.wrap(defaultText.getBytes(StandardCharsets.UTF_8))

    static int defaultDataSize = defaultData.remaining()

    static final Flux<ByteBuffer> defaultFlux = Flux.just(defaultData).map{buffer -> buffer.duplicate()}

    public static final String defaultEndpointTemplate = "http://%s.blob.core.windows.net/"

    @Shared
    protected KB = 1024

    def setupSpec() {
        testMode = setupTestMode()
        primaryCredential = getCredential(PRIMARY_STORAGE)
        alternateCredential = getCredential(SECONDARY_STORAGE)
        blobCredential = getCredential(BLOB_STORAGE)
        premiumCredential = getCredential(PREMIUM_STORAGE)
    }

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        int iterationIndex = fullTestName.lastIndexOf("[")
        int substringIndex = (int) Math.min((iterationIndex != -1) ? iterationIndex : fullTestName.length(), 50)
        this.testName = fullTestName.substring(0, substringIndex)
        this.interceptorManager = new InterceptorManager(className + fullTestName, testMode)
        this.resourceNamer = new TestResourceNamer(className + testName, testMode, interceptorManager.getRecordedData())
        // If the test doesn't have the Requires tag record it in live mode.
        recordLiveMode = specificationContext.getCurrentIteration().getDescription().getAnnotation(Requires.class) == null
    }

    static TestMode setupTestMode() {
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

    def cleanup() {
        interceptorManager.close()
    }

    private SharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (testMode == TestMode.RECORD) {
            accountName = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_NAME")
            accountKey = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_KEY")
        } else {
            accountName = "storageaccount"
            accountKey = "astorageaccountkey"
        }

        if (accountName == null || accountKey == null) {
            logger.warning("Account name or key for the {} account was null. Test's requiring these credentials will fail.", accountType)
            return null
        }

        return new SharedKeyCredential(accountName, accountKey)
    }

    /*
    Size must be an int because ByteBuffer sizes can only be an int. Long is not supported.
    */
    ByteBuffer getRandomData(int size) {
        return ByteBuffer.wrap(getRandomByteArray(size))
    }

    byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(resourceNamer.randomUuid()).getMostSignificantBits() & Long.MAX_VALUE
        Random rand = new Random(seed)
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return data
    }

    /*
    We only allow int because anything larger than 2GB (which would require a long) is left to stress/perf.
     */
    File getRandomFile(int size) {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt")
        file.deleteOnExit()
        FileOutputStream fos = new FileOutputStream(file)
        fos.write(getRandomData(size).array())
        fos.close()
        return file
    }

    static Mono<ByteBuffer> collectBytesInBuffer(Flux<ByteBuffer> content) {
        return FluxUtil.collectBytesInByteBufferStream(content).map { bytes -> ByteBuffer.wrap(bytes) }
    }

    HttpClient getHttpClient() {
        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()
        if (testMode == TestMode.RECORD) {
            builder.wiretap(true)

            if (Boolean.parseBoolean(Configuration.getGlobalConfiguration().get("AZURE_TEST_DEBUGGING"))) {
                builder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            }

            return builder.build()
        } else {
            return interceptorManager.getPlaybackClient()
        }
    }

    EncryptedBlobClientBuilder getEncryptedClientBuilder(IKey key, IKeyResolver keyResolver,
                                                         SharedKeyCredential credential, String endpoint,
                                                         HttpPipelinePolicy... policies) {
        EncryptedBlobClientBuilder builder = new EncryptedBlobClientBuilder()
            .keyAndKeyResolver(key, keyResolver)
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        addOptionalRecording(builder)

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    def addOptionalRecording(BaseClientBuilder<? extends BaseClientBuilder> builder) {
        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }
        return builder
    }

    BlobServiceClientBuilder getServiceClientBuilder(SharedKeyCredential credential, String endpoint,
                                                     HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        addOptionalRecording(builder)

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    def generateContainerName() {
        generateResourceName(containerPrefix, entityNo++)
    }

    private String generateResourceName(String prefix, int entityNo) {
        return resourceNamer.randomName(prefix + testName + entityNo, 63)
    }

    def generateBlobName() {
        generateResourceName(blobPrefix, entityNo++)
    }

    def validateBlobProperties(Response<BlobProperties> response, String cacheControl, String contentDisposition, String contentEncoding,
                               String contentLanguage, byte[] contentMD5, String contentType, boolean checkContentMD5) {

        def propertiesEqual = response.getValue().getCacheControl() == cacheControl &&
            response.getValue().getContentDisposition() == contentDisposition &&
            response.getValue().getContentEncoding() == contentEncoding &&
            response.getValue().getContentLanguage() == contentLanguage &&
            response.getHeaders().getValue("Content-Type") == contentType

        if (checkContentMD5) {
            return propertiesEqual && response.getValue().getContentMD5() == contentMD5
        }
        return propertiesEqual
    }

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to
     * the ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param ebbc
     *      The URL to the blob to get the etag on.
     * @param match
     *      The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is expecting
     *      the blob's actual etag for this test, so it is retrieved.
     * @return
     * The appropriate etag value to run the current test.
     */
    def setupBlobMatchCondition(EncryptedBlockBlobClient ebbc, String match) {
        if (match == receivedEtag) {
            return ebbc.getPropertiesWithResponse(null, null, null).getHeaders().getValue("ETag")
        } else {
            return match
        }
    }

    def setupBlobMatchCondition(EncryptedBlockBlobAsyncClient ebbac, String match) {
        if (match == receivedEtag) {
            return ebbac.getPropertiesWithResponse(null).block().getHeaders().getValue("ETag")
        } else {
            return match
        }
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing leaseAccessConditions. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param bc
     *      The blob on which to acquire a lease.
     * @param leaseID
     *      The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or {@code null}.
     * @return
     * The actual leaseAccessConditions of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    def setupBlobLeaseCondition(BlobClient bc, String leaseID) {
        String responseLeaseId = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            responseLeaseId = createLeaseClient(bc).acquireLease(-1)
        }
        if (leaseID == receivedLeaseID) {
            return responseLeaseId
        } else {
            return leaseID
        }
    }

    def setupBlobLeaseCondition(BlobAsyncClient bac, String leaseID) {
        String responseLeaseId = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            responseLeaseId = new LeaseClientBuilder()
                .blobAsyncClient(bac)
                .buildAsyncClient()
                .acquireLease(-1)
                .block()
        }
        if (leaseID == receivedLeaseID) {
            return responseLeaseId
        } else {
            return leaseID
        }
    }
}
