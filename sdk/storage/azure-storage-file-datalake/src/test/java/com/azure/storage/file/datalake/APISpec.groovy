package com.azure.storage.file.datalake

import com.azure.core.http.*
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.http.rest.Response
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.Configuration
import com.azure.core.util.FluxUtil
import com.azure.core.util.logging.ClientLogger
import com.azure.identity.EnvironmentCredentialBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.file.datalake.models.*
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClient
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.OffsetDateTime
import java.util.function.Supplier

class APISpec extends Specification {
    @Shared
    ClientLogger logger = new ClientLogger(APISpec.class)

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    // both sync and async clients point to same container
    @Shared
    DataLakeFileSystemClient fsc

    @Shared
    DataLakeFileSystemClient fscPremium

    @Shared
    DataLakeFileSystemAsyncClient fscAsync

    // Fields used for conveniently creating blobs with data.
    static final String defaultText = "default"

    public static final ByteBuffer defaultData = ByteBuffer.wrap(defaultText.getBytes(StandardCharsets.UTF_8))

    static final Supplier<InputStream> defaultInputStream = new Supplier<InputStream>() {
        @Override
        InputStream get() {
            return new ByteArrayInputStream(defaultText.getBytes(StandardCharsets.UTF_8))
        }
    }

    static int defaultDataSize = defaultData.remaining()

    static final Flux<ByteBuffer> defaultFlux = Flux.just(defaultData).map { buffer -> buffer.duplicate() }

    // Prefixes for blobs and containers
    String fileSystemPrefix = "jtfs" // java test file system

    String pathPrefix = "javapath"

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

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedLeaseID = "received"

    static final String garbageLeaseID = UUID.randomUUID().toString()

    public static final String defaultEndpointTemplate = "https://%s.dfs.core.windows.net/"

    static def AZURE_TEST_MODE = "AZURE_TEST_MODE"
    static def DATA_LAKE_STORAGE = "STORAGE_DATA_LAKE_"

    protected static StorageSharedKeyCredential primaryCredential
    static StorageSharedKeyCredential alternateCredential
    static StorageSharedKeyCredential pathCredential
    static StorageSharedKeyCredential premiumCredential
    static TestMode testMode

    DataLakeServiceClient primaryDataLakeServiceClient
    DataLakeServiceAsyncClient primaryDataLakeServiceAsyncClient
    DataLakeServiceClient alternateDataLakeServiceClient
    DataLakeServiceClient dataLakeServiceClient
    DataLakeServiceClient premiumDataLakeServiceClient

    InterceptorManager interceptorManager
    boolean recordLiveMode
    private TestResourceNamer resourceNamer
    protected String testName
    def fileSystemName

    def setupSpec() {
        testMode = setupTestMode()
        primaryCredential = getCredential(DATA_LAKE_STORAGE)
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

        primaryDataLakeServiceClient = setClient(primaryCredential)
        primaryDataLakeServiceAsyncClient = getServiceAsyncClient(primaryCredential)
        alternateDataLakeServiceClient = setClient(alternateCredential)
        dataLakeServiceClient = setClient(pathCredential)
        premiumDataLakeServiceClient = setClient(premiumCredential)

        fileSystemName = generateFileSystemName()
        fsc = primaryDataLakeServiceClient.getFileSystemClient(fileSystemName)
        fscAsync = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(fileSystemName)
        fsc.create()
    }

    def cleanup() {
        def options = new ListFileSystemsOptions().setPrefix(fileSystemPrefix + testName)
        for (FileSystemItem fileSystem : primaryDataLakeServiceClient.listFileSystems(options, Duration.ofSeconds(120))) {
            DataLakeFileSystemClient fileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(fileSystem.getName())

            if (fileSystem.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                createLeaseClient(fileSystemClient).breakLeaseWithResponse(0, null, null, null)
            }

            fileSystemClient.delete()
        }

        interceptorManager.close()
    }

    //TODO: Should this go in core.
    static Mono<ByteBuffer> collectBytesInBuffer(Flux<ByteBuffer> content) {
        return FluxUtil.collectBytesInByteBufferStream(content).map { bytes -> ByteBuffer.wrap(bytes) }
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

    static boolean liveMode() {
        return setupTestMode() == TestMode.RECORD
    }

    private StorageSharedKeyCredential getCredential(String accountType) {
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

        return new StorageSharedKeyCredential(accountName, accountKey)
    }

    DataLakeServiceClient setClient(StorageSharedKeyCredential credential) {
        try {
            return getServiceClient(credential)
        } catch (Exception ignore) {
            return null
        }
    }

    def getOAuthServiceClient() {
        DataLakeServiceClientBuilder builder = new DataLakeServiceClientBuilder()
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (testMode == TestMode.RECORD) {
            if (recordLiveMode) {
                builder.addPolicy(interceptorManager.getRecordPolicy())
            }
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            return builder.credential(new EnvironmentCredentialBuilder().build()).buildClient()
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            return builder.credential(primaryCredential).buildClient()
        }
    }

    DataLakeServiceClient getServiceClient(String endpoint) {
        return getServiceClient(null, endpoint, null)
    }

    DataLakeServiceClient getServiceClient(StorageSharedKeyCredential credential) {
        return getServiceClient(credential, String.format(defaultEndpointTemplate, credential.getAccountName()), null)
    }

    DataLakeServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint) {
        return getServiceClient(credential, endpoint, null)
    }

    DataLakeServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
                                           HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient()
    }

    DataLakeServiceClient getServiceClient(String sasToken, String endpoint) {
        return getServiceClientBuilder(null, endpoint, null).sasToken(sasToken).buildClient()
    }

    DataLakeServiceAsyncClient getServiceAsyncClient(StorageSharedKeyCredential credential) {
        return getServiceClientBuilder(credential, String.format(defaultEndpointTemplate, credential.getAccountName()))
            .buildAsyncClient()
    }

    DataLakeServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
                                                     HttpPipelinePolicy... policies) {
        DataLakeServiceClientBuilder builder = new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
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

    static DataLakeLeaseClient createLeaseClient(DataLakeFileClient pathClient) {
        return createLeaseClient(pathClient, null)
    }

    static DataLakeLeaseClient createLeaseClient(DataLakeFileClient pathClient, String leaseId) {
        return new DataLakeLeaseClientBuilder()
            .fileClient(pathClient)
            .leaseId(leaseId)
            .buildClient()
    }

    static DataLakeLeaseClient createLeaseClient(DataLakeDirectoryClient pathClient) {
        return createLeaseClient(pathClient, null)
    }

    static DataLakeLeaseClient createLeaseClient(DataLakeDirectoryClient pathClient, String leaseId) {
        return new DataLakeLeaseClientBuilder()
            .directoryClient(pathClient)
            .leaseId(leaseId)
            .buildClient()
    }

    static DataLakeLeaseClient createLeaseClient(DataLakeFileSystemClient fileSystemClient) {
        return createLeaseClient(fileSystemClient, null)
    }

    static DataLakeLeaseClient createLeaseClient(DataLakeFileSystemClient fileSystemClient, String leaseId) {
        return new DataLakeLeaseClientBuilder()
            .fileSystemClient(fileSystemClient)
            .leaseId(leaseId)
            .buildClient()
    }

    DataLakeFileClient getFileClient(StorageSharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildFileClient()
    }

    DataLakeFileClient getFileClient(StorageSharedKeyCredential credential, String endpoint, String pathName) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .pathName(pathName)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildFileClient()
    }

    DataLakeFileClient getFileClient(String sasToken, String endpoint, String pathName) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .pathName(pathName)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.sasToken(sasToken).buildFileClient()
    }

    DataLakeFileSystemClient getFileSystemClient(String sasToken, String endpoint) {
        getFileSystemClientBuilder(endpoint).sasToken(sasToken).buildClient()
    }

    DataLakeFileSystemClientBuilder getFileSystemClientBuilder(String endpoint) {
        DataLakeFileSystemClientBuilder builder = new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder
    }

    def generateFileSystemName() {
        generateResourceName(fileSystemPrefix, entityNo++)
    }

    def generatePathName() {
        generateResourceName(pathPrefix, entityNo++)
    }

    private String generateResourceName(String prefix, int entityNo) {
        return resourceNamer.randomName(prefix + testName + entityNo, 63)
    }

    String getRandomUUID() {
        return resourceNamer.randomUuid()
    }

    String getConfigValue(String value) {
        return resourceNamer.recordValueFromConfig(value)
    }

    String getBlockID() {
        return Base64.encoder.encodeToString(resourceNamer.randomUuid().getBytes(StandardCharsets.UTF_8))
    }

    OffsetDateTime getUTCNow() {
        return resourceNamer.now()
    }

    byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(resourceNamer.randomUuid()).getMostSignificantBits() & Long.MAX_VALUE
        Random rand = new Random(seed)
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return data
    }

    /*
    Size must be an int because ByteBuffer sizes can only be an int. Long is not supported.
     */

    ByteBuffer getRandomData(int size) {
        return ByteBuffer.wrap(getRandomByteArray(size))
    }

    /**
     * Validates the presence of headers that are present on a large number of responses. These headers are generally
     * random and can really only be checked as not null.
     * @param headers
     *      The object (may be headers object or response object) that has properties which expose these common headers.
     * @return
     * Whether or not the header values are appropriate.
     */
    def validateBasicHeaders(HttpHeaders headers) {
        return headers.getValue("etag") != null &&
            // Quotes should be scrubbed from etag header values
            !headers.getValue("etag").contains("\"") &&
            headers.getValue("last-modified") != null &&
            headers.getValue("x-ms-request-id") != null &&
            headers.getValue("x-ms-version") != null &&
            headers.getValue("date") != null
    }

    def validatePathProperties(Response<PathProperties> response, String cacheControl, String contentDisposition,
                               String contentEncoding, String contentLanguage, byte[] contentMD5, String contentType) {
        return response.getValue().getCacheControl() == cacheControl &&
            response.getValue().getContentDisposition() == contentDisposition &&
            response.getValue().getContentEncoding() == contentEncoding &&
            response.getValue().getContentLanguage() == contentLanguage &&
            response.getValue().getContentMd5() == contentMD5 &&
            response.getHeaders().getValue("Content-Type") == contentType
    }

    def setupFileSystemLeaseCondition(DataLakeFileSystemClient fsc, String leaseID) {
        if (leaseID == receivedLeaseID) {
            return createLeaseClient(fsc).acquireLease(-1)
        } else {
            return leaseID
        }
    }

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to
     * the ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param bc
     *      The URL to the path to get the etag on.
     * @param match
     *      The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is expecting
     *      the path's actual etag for this test, so it is retrieved.
     * @return
     * The appropriate etag value to run the current test.
     */
    def setupPathMatchCondition(DataLakePathClient pc, String match) {
        if (match == receivedEtag) {
            return pc.getProperties().getETag()
        } else {
            return match
        }
    }

    def setupPathMatchCondition(DataLakePathAsyncClient pac, String match) {
        if (match == receivedEtag) {
            return pac.getProperties().block().getETag()
        } else {
            return match
        }
    }

    /**
     * This helper method will acquire a lease on a path to prepare for testing lease id. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param bc
     *      The path on which to acquire a lease.
     * @param leaseID
     *      The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or {@code null}.
     * @return
     * The actual lease id of the path if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    def setupPathLeaseCondition(DataLakePathClient pc, String leaseID) {
        String responseLeaseId = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            responseLeaseId = createLeaseClient(pc).acquireLease(-1)
        }
        if (leaseID == receivedLeaseID) {
            return responseLeaseId
        } else {
            return leaseID
        }
    }

    def setupPathLeaseCondition(DataLakePathAsyncClient pac, String leaseID) {
        String responseLeaseId = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            responseLeaseId = new DataLakeLeaseClientBuilder()
                .pathAsyncClient(pac)
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

    class MockRetryRangeResponsePolicy implements HttpPipelinePolicy {
        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().flatMap { HttpResponse response ->
                if (response.getRequest().getHeaders().getValue("x-ms-range") != "bytes=2-6") {
                    return Mono.<HttpResponse> error(new IllegalArgumentException("The range header was not set correctly on retry."))
                } else {
                    // ETag can be a dummy value. It's not validated, but DownloadResponse requires one
                    return Mono.<HttpResponse> just(new MockDownloadHttpResponse(response, 206, Flux.error(new IOException())))
                }
            }
        }
    }

    /*
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it. Because this type is just for BlobDownload, we don't need to accept a header type.
     */

    class MockDownloadHttpResponse extends HttpResponse {
        private final int statusCode
        private final HttpHeaders headers
        private final Flux<ByteBuffer> body

        MockDownloadHttpResponse(HttpResponse response, int statusCode, Flux<ByteBuffer> body) {
            super(response.getRequest())
            this.statusCode = statusCode
            this.headers = response.getHeaders()
            this.body = body
        }

        @Override
        int getStatusCode() {
            return statusCode
        }

        @Override
        String getHeaderValue(String s) {
            return headers.getValue(s)
        }

        @Override
        HttpHeaders getHeaders() {
            return headers
        }

        @Override
        Flux<ByteBuffer> getBody() {
            return body
        }

        @Override
        Mono<byte[]> getBodyAsByteArray() {
            return Mono.error(new IOException())
        }

        @Override
        Mono<String> getBodyAsString() {
            return Mono.error(new IOException())
        }

        @Override
        Mono<String> getBodyAsString(Charset charset) {
            return Mono.error(new IOException())
        }
    }

    // Only sleep if test is running in live mode
    def sleepIfRecord(long milliseconds) {
        if (testMode == TestMode.RECORD) {
            sleep(milliseconds)
        }
    }

    def compareACL(List<PathAccessControlEntry> expected, List<PathAccessControlEntry> actual) {
        if (expected.size() == actual.size()) {
            boolean success = true
            for (PathAccessControlEntry entry : expected) {
                success = success && entryIsInAcl(entry, actual)
            }
            return success
        }
        return false

    }

    def entryIsInAcl(PathAccessControlEntry entry, List<PathAccessControlEntry> acl) {
        for (PathAccessControlEntry e : acl) {
            if (e.defaultScope() == entry.defaultScope() &&
                e.accessControlType().equals(entry.accessControlType()) &&
                (e.entityID() == null && entry.entityID() == null ||
                    e.entityID().equals(entry.entityID())) &&
                e.permissions().equals(entry.permissions())) {
                return true
            }
        }
        return false
    }

    def sleepIfLive(long milliseconds) {
        if (testMode == TestMode.PLAYBACK) {
            return
        }
        sleep(milliseconds)
    }

}
