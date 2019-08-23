// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpClient
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.ProxyOptions
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.http.rest.Response
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.core.util.logging.ClientLogger
import com.azure.identity.credential.EnvironmentCredentialBuilder
import com.azure.storage.blob.BlobProperties
import com.azure.storage.blob.models.ContainerItem
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.ListContainersOptions
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.RetentionPolicy
import com.azure.storage.blob.models.StorageServiceProperties
import com.azure.storage.common.Constants
import com.azure.storage.common.credentials.SASTokenCredential
import com.azure.storage.common.credentials.SharedKeyCredential
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.OffsetDateTime
import java.util.function.Supplier

class APISpec extends Specification {
    private final ClientLogger logger = new ClientLogger(APISpec.class)

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    @Shared
    ContainerClient cu

    // Fields used for conveniently creating blobs with data.
    static final String defaultText = "default"

    static final ByteBuffer defaultData = ByteBuffer.wrap(defaultText.getBytes(StandardCharsets.UTF_8))

    static final Supplier<InputStream> defaultInputStream = new Supplier<InputStream>() {
        @Override
        InputStream get() {
            return new ByteArrayInputStream(defaultText.getBytes(StandardCharsets.UTF_8))
        }
    }

    static int defaultDataSize = defaultData.remaining()

    // Prefixes for blobs and containers
    String containerPrefix = "jtc" // java test container

    String blobPrefix = "javablob"

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

    static def AZURE_TEST_MODE = "AZURE_TEST_MODE"
    static def PRIMARY_STORAGE = "PRIMARY_STORAGE_"
    static def SECONDARY_STORAGE = "SECONDARY_STORAGE_"
    static def BLOB_STORAGE = "BLOB_STORAGE_"
    static def PREMIUM_STORAGE = "PREMIUM_STORAGE_"

    static SharedKeyCredential primaryCredential
    static SharedKeyCredential alternateCredential
    static SharedKeyCredential blobCredential
    static SharedKeyCredential premiumCredential
    static TestMode testMode

    BlobServiceClient primaryServiceClient
    BlobServiceClient alternateServiceClient
    BlobServiceClient blobStorageServiceClient
    BlobServiceClient premiumServiceClient

    private InterceptorManager interceptorManager
    private TestResourceNamer resourceNamer
    private String testName

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

        primaryServiceClient = setClient(primaryCredential)
        alternateServiceClient = setClient(alternateCredential)
        blobStorageServiceClient = setClient(blobCredential)
        premiumServiceClient = setClient(premiumCredential)

        cu = primaryServiceClient.getContainerClient(generateContainerName())
        cu.create()
    }

    def cleanup() {
        for (ContainerItem container : primaryServiceClient.listContainers(new ListContainersOptions()
            .prefix(containerPrefix + testName), Duration.ofSeconds(120))) {
            ContainerClient containerClient = primaryServiceClient.getContainerClient(container.name())

            if (container.properties().leaseState() == LeaseStateType.LEASED) {
                containerClient.breakLeaseWithResponse(0, null, null, null)
            }

            containerClient.delete()
        }

        interceptorManager.close()
    }

    static TestMode setupTestMode() {
        String testMode = ConfigurationManager.getConfiguration().get(AZURE_TEST_MODE)

        if (testMode != null) {
            try {
                return TestMode.valueOf(testMode.toUpperCase(Locale.US))
            } catch (IllegalArgumentException ex) {
                return TestMode.PLAYBACK
            }
        }

        return TestMode.PLAYBACK
    }

    static boolean liveMode() {
        return setupTestMode() == TestMode.RECORD
    }

    private SharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (testMode == TestMode.RECORD) {
            accountName = ConfigurationManager.getConfiguration().get(accountType + "ACCOUNT_NAME")
            accountKey = ConfigurationManager.getConfiguration().get(accountType + "ACCOUNT_KEY")
        } else {
            accountName = "storageaccount"
            accountKey = "storageaccountkey"
        }

        if (accountName == null || accountKey == null) {
            logger.warning("Account name or key for the {} account was null. Test's requiring these credentials will fail.", accountType)
            return null
        }

        return new SharedKeyCredential(accountName, accountKey)
    }

    BlobServiceClient setClient(SharedKeyCredential credential) {
        try {
            return getServiceClient(credential)
        } catch (Exception ex) {
            return null
        }
    }

    def getOAuthServiceClient() {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(String.format("https://%s.blob.core.windows.net/", primaryCredential.accountName()))
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())

            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            return builder.credential(new EnvironmentCredentialBuilder().build()).buildClient()
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            return builder.credential(primaryCredential).buildClient()
        }
    }

    BlobServiceClient getServiceClient(String endpoint) {
        return getServiceClient(null, endpoint, null)
    }

    BlobServiceClient getServiceClient(SharedKeyCredential credential) {
        return getServiceClient(credential, String.format("https://%s.blob.core.windows.net", credential.accountName()), null)
    }

    BlobServiceClient getServiceClient(SharedKeyCredential credential, String endpoint) {
        return getServiceClient(credential, endpoint, null)
    }

    BlobServiceClient getServiceClient(SharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        if (credential != null) {
            builder.credential(credential)
        }

        return builder.buildClient()
    }

    BlobServiceClient getServiceClient(SASTokenCredential credential, String endpoint) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildClient()
    }

    ContainerClient getContainerClient(SASTokenCredential credential, String endpoint) {
        ContainerClientBuilder builder = new ContainerClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        builder.credential(credential).buildClient()
    }

    BlobAsyncClient getBlobAsyncClient(SharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        builder.credential(credential).buildBlobAsyncClient()
    }

    BlobClient getBlobClient(SASTokenCredential credential, String endpoint, String blobName) {
        return getBlobClient(credential, endpoint, blobName, null)
    }

    BlobClient getBlobClient(SASTokenCredential credential, String endpoint, String blobName, String snapshotId) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .snapshot(snapshotId)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildBlobClient()
    }

    BlobClient getBlobClient(SharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildBlobClient()
    }

    BlobClient getBlobClient(SharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildBlobClient()
    }

    BlobClient getBlobClient(String endpoint, SASTokenCredential credential) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (credential != null) {
            builder.credential(credential)
        }

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.buildBlobClient()
    }

    private HttpClient getHttpClient() {
        HttpClient client
        if (testMode == TestMode.RECORD) {
            client = HttpClient.createDefault().wiretap(true)
        } else {
            client = interceptorManager.getPlaybackClient()
        }

        if (Boolean.parseBoolean(ConfigurationManager.getConfiguration().get("AZURE_TEST_DEBUGGING"))) {
            return client.proxy(PROXY_OPTIONS)
        } else {
            return client
        }
    }

    private Supplier<ProxyOptions> PROXY_OPTIONS = new Supplier<ProxyOptions>() {
        @Override
        ProxyOptions get() {
            return new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
        }
    }

    def generateContainerName() {
        generateResourceName(containerPrefix, entityNo++)
    }

    def generateBlobName() {
        generateResourceName(blobPrefix, entityNo++)
    }

    private String generateResourceName(String prefix, int entityNo) {
        return resourceNamer.randomName(prefix + testName + entityNo, 63)
    }

    String getRandomUUID() {
        return resourceNamer.randomUuid()
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

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to
     * the ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param bu
     *      The URL to the blob to get the etag on.
     * @param match
     *      The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is expecting
     *      the blob's actual etag for this test, so it is retrieved.
     * @return
     * The appropriate etag value to run the current test.
     */
    def setupBlobMatchCondition(BlobClient bu, String match) {
        if (match == receivedEtag) {
            bu.getPropertiesWithResponse(null, null, null).headers().value("ETag")
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
     * @param bu
     *      The blob on which to acquire a lease.
     * @param leaseID
     *      The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or {@code null}.
     * @return
     * The actual leaseAccessConditions of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    def setupBlobLeaseCondition(BlobClient bu, String leaseID) {
        String responseLeaseId = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            responseLeaseId = bu.acquireLease(null, -1)
        }

        return (leaseID == receivedLeaseID) ? responseLeaseId : leaseID
    }

    def setupContainerMatchCondition(ContainerClient cu, String match) {
        if (match == receivedEtag) {
            return cu.getPropertiesWithResponse(null, null, null).headers().value("ETag")
        } else {
            return match
        }
    }

    def setupContainerLeaseCondition(ContainerClient cu, String leaseID) {
        if (leaseID == receivedLeaseID) {
            return cu.acquireLeaseWithResponse(null, -1, null, null, null).value()
        } else {
            return leaseID
        }
    }

    def getMockRequest() {
        HttpHeaders headers = new HttpHeaders()
        headers.put(Constants.HeaderConstants.CONTENT_ENCODING, "en-US")
        URL url = new URL("http://devtest.blob.core.windows.net/test-container/test-blob")
        HttpRequest request = new HttpRequest(HttpMethod.POST, url, headers, null)
        return request
    }

    def waitForCopy(ContainerClient bu, String status) {
        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = bu.getPropertiesWithResponse(null, null, null).headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleepIfRecord(1000)
        }
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
        return headers.value("etag") != null &&
            // Quotes should be scrubbed from etag header values
            !headers.value("etag").contains("\"") &&
            headers.value("last-modified") != null &&
            headers.value("x-ms-request-id") != null &&
            headers.value("x-ms-version") != null &&
            headers.value("date") != null
    }

    def validateBlobProperties(Response<BlobProperties> response, String cacheControl, String contentDisposition, String contentEncoding,
                               String contentLanguage, byte[] contentMD5, String contentType) {
        return response.value().cacheControl() == cacheControl &&
            response.value().contentDisposition() == contentDisposition &&
            response.value().contentEncoding() == contentEncoding &&
            response.value().contentLanguage() == contentLanguage &&
            response.value().contentMD5() == contentMD5 &&
            response.headers().value("Content-Type") == contentType
    }

    Metadata getMetadataFromHeaders(HttpHeaders headers) {
        Metadata metadata = new Metadata()

        for (Map.Entry<String, String> header : headers.toMap()) {
            if (header.getKey().startsWith("x-ms-meta-")) {
                String metadataKey = header.getKey().substring(10)
                metadata.put(metadataKey, header.getValue())
            }
        }

        return metadata
    }

    def enableSoftDelete() {
        primaryServiceClient.setProperties(new StorageServiceProperties()
            .deleteRetentionPolicy(new RetentionPolicy().enabled(true).days(2)))

        sleepIfRecord(30000)
    }

    def disableSoftDelete() {
        primaryServiceClient.setProperties(new StorageServiceProperties()
            .deleteRetentionPolicy(new RetentionPolicy().enabled(false)))

        sleepIfRecord(30000)
    }

    // Only sleep if test is running in live mode
    def sleepIfRecord(long milliseconds) {
        if (testMode == TestMode.RECORD) {
            sleep(milliseconds)
        }
    }

    class MockRetryRangeResponsePolicy implements HttpPipelinePolicy {
        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().flatMap { HttpResponse response ->
                if (response.request().headers().value("x-ms-range") != "bytes=2-6") {
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
            this.request(response.request())
            this.statusCode = statusCode
            this.headers = response.headers()
            this.body = body
        }

        @Override
        int statusCode() {
            return statusCode
        }

        @Override
        String headerValue(String s) {
            return headers.value(s)
        }

        @Override
        HttpHeaders headers() {
            return headers
        }

        @Override
        Flux<ByteBuffer> body() {
            return body
        }

        @Override
        Mono<byte[]> bodyAsByteArray() {
            return Mono.error(new IOException())
        }

        @Override
        Mono<String> bodyAsString() {
            return Mono.error(new IOException())
        }

        @Override
        Mono<String> bodyAsString(Charset charset) {
            return Mono.error(new IOException())
        }
    }
}
