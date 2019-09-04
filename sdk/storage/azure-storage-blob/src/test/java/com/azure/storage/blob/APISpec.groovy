// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.http.rest.Response
import com.azure.core.implementation.util.FluxUtil
import com.azure.core.test.TestMode
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.core.util.logging.ClientLogger
import com.azure.identity.credential.EnvironmentCredentialBuilder
import com.azure.storage.blob.models.ContainerItem
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.ListContainersOptions
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.RetentionPolicy
import com.azure.storage.blob.models.StorageServiceProperties
import com.azure.storage.common.Constants
import com.azure.storage.common.TestBase
import com.azure.storage.common.credentials.SASTokenCredential
import com.azure.storage.common.credentials.SharedKeyCredential
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Shared

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.OffsetDateTime
import java.util.function.Supplier

class APISpec extends TestBase {
    @Shared
    ClientLogger logger = new ClientLogger(APISpec.class)

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    // both sync and async clients point to same container
    @Shared
    ContainerClient cc

    @Shared
    ContainerAsyncClient ccAsync

    // Fields used for conveniently creating blobs with data.
    static final String defaultText = "default"

    public static final ByteBuffer defaultData = ByteBuffer.wrap(defaultText.getBytes(StandardCharsets.UTF_8))

    static final Supplier<InputStream> defaultInputStream = new Supplier<InputStream>(){
        @Override
        InputStream get() {
            return new ByteArrayInputStream(defaultText.getBytes(StandardCharsets.UTF_8))
        }
    }

    static int defaultDataSize = defaultData.remaining()

    static final Flux<ByteBuffer> defaultFlux = Flux.just(defaultData).map{buffer -> buffer.duplicate()}

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

    public static final String defaultEndpointTemplate = "https://%s.blob.core.windows.net/"

    static def PRIMARY_STORAGE = "PRIMARY_STORAGE_"
    static def SECONDARY_STORAGE = "SECONDARY_STORAGE_"
    static def BLOB_STORAGE = "BLOB_STORAGE_"
    static def PREMIUM_STORAGE = "PREMIUM_STORAGE_"

    static SharedKeyCredential primaryCredential
    static SharedKeyCredential alternateCredential
    static SharedKeyCredential blobCredential
    static SharedKeyCredential premiumCredential

    BlobServiceClient primaryBlobServiceClient
    BlobServiceAsyncClient primaryBlobServiceAsyncClient
    BlobServiceClient alternateBlobServiceClient
    BlobServiceClient blobServiceClient
    BlobServiceClient premiumBlobServiceClient

    def setupSpec() {
        primaryCredential = getCredential(PRIMARY_STORAGE)
        alternateCredential = getCredential(SECONDARY_STORAGE)
        blobCredential = getCredential(BLOB_STORAGE)
        premiumCredential = getCredential(PREMIUM_STORAGE)
    }

    def setup() {
        primaryBlobServiceClient = setClient(primaryCredential)
        primaryBlobServiceAsyncClient = getServiceAsyncClient(primaryCredential)
        alternateBlobServiceClient = setClient(alternateCredential)
        blobServiceClient = setClient(blobCredential)
        premiumBlobServiceClient = setClient(premiumCredential)

        def containerName = generateContainerName()
        cc = primaryBlobServiceClient.getContainerClient(containerName)
        ccAsync = primaryBlobServiceAsyncClient.getContainerAsyncClient(containerName)
        cc.create()
    }

    def cleanup() {
        def options = new ListContainersOptions().prefix(containerPrefix + testName)
        for (ContainerItem container : primaryBlobServiceClient.listContainers(options, Duration.ofSeconds(120))) {
            ContainerClient containerClient = primaryBlobServiceClient.getContainerClient(container.name())

            if (container.properties().leaseState() == LeaseStateType.LEASED) {
                containerClient.breakLeaseWithResponse(0, null, null, null)
            }

            containerClient.delete()
        }
    }

    //TODO: Should this go in core.
     static Mono<ByteBuffer> collectBytesInBuffer(Flux<ByteBuffer> content) {
         return FluxUtil.collectBytesInByteBufferStream(content).map{bytes -> ByteBuffer.wrap(bytes)}
    }

    private SharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (testMode == TestMode.RECORD) {
            accountName = ConfigurationManager.getConfiguration().get(accountType + "ACCOUNT_NAME")
            accountKey = ConfigurationManager.getConfiguration().get(accountType + "ACCOUNT_KEY")
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

    BlobServiceClient setClient(SharedKeyCredential credential) {
        try {
            return setupBlobServiceClientBuilder(String.format(defaultEndpointTemplate, credential.accountName()))
                .credential(credential)
                .buildClient()
        } catch (Exception ignore) {
            logger.info("Failed to configure client for {}", credential.accountName())
            return null
        }
    }

    def setupBlobServiceClientBuilder(String endpoint, HttpPipelinePolicy... policies) {
        return setupBuilder(new BlobServiceClientBuilder(), endpoint, policies)
    }

    def setupBlobClientBuilder(String endpoint, HttpPipelinePolicy... policies) {
        return setupBuilder(new BlobClientBuilder(), endpoint, policies)
    }

    def getOAuthServiceClient() {
        def endpoint = String.format(defaultEndpointTemplate, primaryCredential.accountName())
        BlobServiceClientBuilder builder = setupBlobServiceClientBuilder(endpoint)

        if (testMode == TestMode.RECORD) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            return builder.credential(new EnvironmentCredentialBuilder().build()).buildClient()
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            return builder.credential(primaryCredential).buildClient()
        }
    }

    BlobServiceAsyncClient getServiceAsyncClient(SharedKeyCredential credential) {
        return setupBlobServiceClientBuilder(String.format(defaultEndpointTemplate, credential.accountName()))
            .credential(credential)
            .buildAsyncClient()
    }

    ContainerClient getContainerClient(SASTokenCredential credential, String endpoint) {
        return setupBuilder(new ContainerClientBuilder(), endpoint)
            .credential(credential)
            .buildClient()
    }

    def generateContainerName() {
        generateResourceName(containerPrefix + entityNo++, 63)
    }

    def generateBlobName() {
        generateResourceName(blobPrefix + entityNo++, 63)
    }

    def getBlockID() {
        return Base64.encoder.encodeToString(generateRandomUUID().getBytes(StandardCharsets.UTF_8))
    }

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to
     * the ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param bc
     *      The URL to the blob to get the etag on.
     * @param match
     *      The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is expecting
     *      the blob's actual etag for this test, so it is retrieved.
     * @return
     * The appropriate etag value to run the current test.
     */
    def setupBlobMatchCondition(BlobClient bc, String match) {
        if (match == receivedEtag) {
            return bc.getPropertiesWithResponse(null, null, null).headers().value("ETag")
        } else {
            return match
        }
    }

    def setupBlobMatchCondition(BlobAsyncClient bac, String match) {
        if (match == receivedEtag) {
            return bac.getPropertiesWithResponse(null, null).block().headers().value("ETag")
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
            responseLeaseId = bc.acquireLease(null, -1)
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
            responseLeaseId = bac.acquireLease(null, -1).block()
        }
        if (leaseID == receivedLeaseID) {
            return responseLeaseId
        } else {
            return leaseID
        }
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
        primaryBlobServiceClient.setProperties(new StorageServiceProperties()
            .deleteRetentionPolicy(new RetentionPolicy().enabled(true).days(2)))

        sleepIfRecord(30000)
    }

    def disableSoftDelete() {
        primaryBlobServiceClient.setProperties(new StorageServiceProperties()
            .deleteRetentionPolicy(new RetentionPolicy().enabled(false)))

        sleepIfRecord(30000)
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
