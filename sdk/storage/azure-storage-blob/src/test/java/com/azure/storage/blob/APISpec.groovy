// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.*
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.http.rest.Response
import com.azure.core.test.TestMode
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.models.*
import com.azure.storage.common.credentials.SharedKeyCredential
import io.netty.buffer.ByteBuf
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

    @Shared
    protected TestCommon testCommon

    static SharedKeyCredential primaryCredential
    static SharedKeyCredential alternateCredential
    static SharedKeyCredential blobCredential
    static SharedKeyCredential premiumCredential

    BlobServiceClient primaryServiceClient
    BlobServiceClient alternateServiceClient
    BlobServiceClient blobStorageServiceClient
    BlobServiceClient premiumServiceClient

    def setupSpec() {
        primaryCredential = getCredential("PRIMARY_STORAGE_")
        alternateCredential = getCredential("SECONDARY_STORAGE_")
        blobCredential = getCredential("BLOB_STORAGE_")
        premiumCredential = getCredential("PREMIUM_STORAGE_")
    }

    def setup() {
        String testName = specificationContext.getCurrentFeature().getName().replace(' ', '').toLowerCase()
        boolean appendIteration = specificationContext.currentIteration.estimatedNumIterations > 1

        Integer iterationNo = 0
        if (appendIteration) {
            iterationNo = unrollIterationNo.get(testName)
            if (iterationNo == null) {
                iterationNo = 0
                unrollIterationNo.put(testName, iterationNo)
            } else {
                unrollIterationNo.put(testName, ++iterationNo)
            }
        }

        testCommon = new TestCommon(testName.substring(0, (int) Math.min(testName.length(), 32)), appendIteration, iterationNo)

        primaryServiceClient = testCommon.setClient(primaryCredential)
        alternateServiceClient = testCommon.setClient(alternateCredential)
        blobStorageServiceClient = testCommon.setClient(blobCredential)
        premiumServiceClient = testCommon.setClient(premiumCredential)

        cu = primaryServiceClient.getContainerClient(generateContainerName())
        cu.create()
    }

    def cleanup() {
        for (ContainerItem container : primaryServiceClient.listContainers(new ListContainersOptions()
            .prefix(containerPrefix + testCommon.getTestName()), Duration.ofSeconds(120))) {
            ContainerClient containerClient = primaryServiceClient.getContainerClient(container.name())

            if (container.properties().leaseState() == LeaseStateType.LEASED) {
                containerClient.breakLease(0, null, null)
            }

            containerClient.delete()
        }

        testCommon.stopRecording()
    }

    private SharedKeyCredential getCredential(String accountType) {
        String accountName = ConfigurationManager.getConfiguration().get(accountType + "ACCOUNT_NAME")
        String accountKey = ConfigurationManager.getConfiguration().get(accountType + "ACCOUNT_KEY")

        if (accountName == null || accountKey == null) {
            logger.warning("Account name or key for the {} account was null. Test's requiring these credentials will fail.", accountType)
            return null
        }

        return new SharedKeyCredential(accountName, accountKey)
    }

    def getOAuthServiceURL() {
        return testCommon.getOAuthServiceClient(primaryCredential.accountName())
    }

    // Mapping of stable container names for recording tests with multiple iterations
    @Shared
    Map<String, Integer> unrollIterationNo = new HashMap<>()

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

    /*
    Constants for testing that the context parameter is properly passed to the pipeline.
     */
    final String defaultContextKey = "Key"

    def generateContainerName() {
        testCommon.generateResourceName(containerPrefix, entityNo++)
    }

    def generateBlobName() {
        testCommon.generateResourceName(blobPrefix, entityNo++)
    }

    static byte[] getRandomByteArray(int size) {
        Random rand = new Random(System.currentTimeMillis())
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return data
    }

    /*
    Size must be an int because ByteBuffer sizes can only be an int. Long is not supported.
     */

    static ByteBuffer getRandomData(int size) {
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
        return (match == receivedEtag) ? bu.getProperties().headers().value("ETag") : match
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
            responseLeaseId = bu.acquireLease(null, -1, null, null).value()
        }

        return (leaseID == receivedLeaseID) ? responseLeaseId : leaseID
    }

    def setupContainerMatchCondition(ContainerClient cu, String match) {
        return (match == receivedEtag) ? cu.getProperties().headers().value("ETag") : match
    }

    def setupContainerLeaseCondition(ContainerClient cu, String leaseID) {
        return (leaseID == receivedLeaseID) ? cu.acquireLease(null, -1).value() : leaseID
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
            status = bu.getProperties().headers().value("x-ms-copy-status")
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
        if (testCommon.getTestMode() == TestMode.RECORD) {
            sleep(milliseconds)
        }
    }

    /*
    This method returns a stub of an HttpResponse. This is for when we want to test policies in isolation but don't care
     about the status code, so we stub a response that always returns a given value for the status code. We never care
     about the number or nature of interactions with this stub.
     */

    def getStubResponse(int code) {
        return Stub(HttpResponse) {
            statusCode() >> code
        }
    }

    /*
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it.
     */

    def getStubResponse(int code, HttpRequest request) {
        return new HttpResponse() {

            @Override
            int statusCode() {
                return code
            }

            @Override
            String headerValue(String s) {
                return null
            }

            @Override
            HttpHeaders headers() {
                return new HttpHeaders()
            }

            @Override
            Flux<ByteBuffer> body() {
                return Flux.empty()
            }

            @Override
            Mono<byte[]> bodyAsByteArray() {
                return Mono.just(new byte[0])
            }

            @Override
            Mono<String> bodyAsString() {
                return Mono.just("")
            }

            @Override
            Mono<String> bodyAsString(Charset charset) {
                return Mono.just("")
            }
        }.request(request)
    }

    class MockRetryRangeResponsePolicy implements HttpPipelinePolicy {
        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().flatMap { HttpResponse response ->
                if (response.request().headers().value("x-ms-range") != "bytes=2-6") {
                    return Mono.<HttpResponse>error(new IllegalArgumentException("The range header was not set correctly on retry."))
                } else {
                    // ETag can be a dummy value. It's not validated, but DownloadResponse requires one
                    return Mono.<HttpResponse>just(new MockDownloadHttpResponse(response, 206, Flux.error(new IOException())))
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
        private final Flux<ByteBuf> body

        MockDownloadHttpResponse(HttpResponse response, int statusCode, Flux<ByteBuf> body) {
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
        Flux<ByteBuf> body() {
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

    def getContextStubPolicy(int successCode, Class responseHeadersType) {
        return Mock(HttpPipelinePolicy) {
            process(_ as HttpPipelineCallContext, _ as HttpPipelineNextPolicy) >> {
                HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
                    if (!context.getData(defaultContextKey).isPresent()) {
                        return Mono.error(new RuntimeException("Context key not present."))
                    } else {
                        return Mono.just(getStubResponse(successCode, context.httpRequest()))
                    }
            }
        }
    }
}
