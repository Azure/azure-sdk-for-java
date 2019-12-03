// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.http.rest.Response
import com.azure.core.test.TestMode
import com.azure.core.util.CoreUtils
import com.azure.identity.EnvironmentCredentialBuilder
import com.azure.storage.blob.models.BlobContainerItem
import com.azure.storage.blob.models.BlobProperties
import com.azure.storage.blob.models.BlobRetentionPolicy
import com.azure.storage.blob.models.BlobServiceProperties
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.ListBlobContainersOptions
import com.azure.storage.blob.specialized.BlobAsyncClientBase
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.blob.specialized.BlobLeaseClient
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder
import com.azure.storage.common.StorageMockHttpResponse
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.StorageTestBase
import com.azure.storage.common.implementation.Constants
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Shared

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.OffsetDateTime
import java.util.function.Supplier

class APISpec extends StorageTestBase {
    // both sync and async clients point to same container
    @Shared
    BlobContainerClient cc

    @Shared
    BlobContainerClient ccPremium

    @Shared
    BlobContainerAsyncClient ccAsync

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

    protected static StorageSharedKeyCredential primaryCredential
    static StorageSharedKeyCredential alternateCredential
    static StorageSharedKeyCredential blobCredential
    static StorageSharedKeyCredential premiumCredential

    BlobServiceClient primaryBlobServiceClient
    BlobServiceAsyncClient primaryBlobServiceAsyncClient
    BlobServiceClient alternateBlobServiceClient
    BlobServiceClient blobServiceClient
    BlobServiceClient premiumBlobServiceClient

    String containerName

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

        containerName = generateContainerName()
        cc = primaryBlobServiceClient.getBlobContainerClient(containerName)
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
        cc.create()
    }

    def cleanup() {
        def options = new ListBlobContainersOptions().setPrefix(containerPrefix + testName)
        for (BlobContainerItem container : primaryBlobServiceClient.listBlobContainers(options, Duration.ofSeconds(120))) {
            BlobContainerClient containerClient = primaryBlobServiceClient.getBlobContainerClient(container.getName())

            if (container.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                createLeaseClient(containerClient).breakLeaseWithResponse(0, null, null, null)
            }

            containerClient.delete()
        }

        interceptorManager.close()
    }

    BlobServiceClient setClient(StorageSharedKeyCredential credential) {
        try {
            return getServiceClient(credential)
        } catch (Exception ignore) {
            return null
        }
    }

    def getOAuthServiceClient() {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
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

    BlobServiceClient getServiceClient(String endpoint) {
        return getServiceClient(null, endpoint, null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential) {
        return getServiceClient(credential, String.format(defaultEndpointTemplate, credential.getAccountName()), null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint) {
        return getServiceClient(credential, endpoint, null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient()
    }

    BlobServiceClient getServiceClient(String sasToken, String endpoint) {
        return getServiceClientBuilder(null, endpoint, null).sasToken(sasToken).buildClient()
    }

    BlobServiceAsyncClient getServiceAsyncClient(StorageSharedKeyCredential credential) {
        return getServiceClientBuilder(credential, String.format(defaultEndpointTemplate, credential.getAccountName()))
            .buildAsyncClient()
    }

    BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
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

    BlobContainerClient getContainerClient(String sasToken, String endpoint) {
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        builder.sasToken(sasToken).buildClient()
    }

    BlobAsyncClient getBlobAsyncClient(StorageSharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        builder.credential(credential).buildAsyncClient()
    }

    BlobClient getBlobClient(String sasToken, String endpoint, String blobName) {
        return getBlobClient(sasToken, endpoint, blobName, null)
    }

    BlobClient getBlobClient(String sasToken, String endpoint, String blobName, String snapshotId) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .snapshot(snapshotId)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.sasToken(sasToken).buildClient()
    }

    BlobClient getBlobClient(StorageSharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildClient()
    }

    BlobClient getBlobClient(StorageSharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildClient()
    }

    BlobClient getBlobClient(String endpoint, String sasToken) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (!CoreUtils.isNullOrEmpty(sasToken)) {
            builder.sasToken(sasToken)
        }

        if (testMode == TestMode.RECORD && recordLiveMode) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.buildClient()
    }

    static BlobLeaseClient createLeaseClient(BlobClientBase blobClient) {
        return createLeaseClient(blobClient, null)
    }

    static BlobLeaseClient createLeaseClient(BlobClientBase blobClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient()
    }

    static BlobLeaseClient createLeaseClient(BlobContainerClient containerClient) {
        return createLeaseClient(containerClient, null)
    }

    static BlobLeaseClient createLeaseClient(BlobContainerClient containerClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .containerClient(containerClient)
            .leaseId(leaseId)
            .buildClient()
    }

    def generateContainerName() {
        generateResourceName(containerPrefix + testName, 63)
    }

    def generateBlobName() {
        generateResourceName(blobPrefix + testName, 63)
    }

    String getBlockID() {
        return Base64.encoder.encodeToString(resourceNamer.randomUuid().getBytes(StandardCharsets.UTF_8))
    }

    /**
     * Compares two files for having equivalent content.
     *
     * @param file1 File used to upload data to the service
     * @param file2 File used to download data from the service
     * @param offset Write offset from the upload file
     * @param count Size of the download from the service
     * @return Whether the files have equivalent content based on offset and read count
     */
    def compareFiles(File file1, File file2, long offset, long count) {
        def pos = 0L
        def readBuffer = 8 * Constants.KB
        def fileChannel1 = AsynchronousFileChannel.open(file1.toPath())
        def fileChannel2 = AsynchronousFileChannel.open(file2.toPath())

        while (pos < count) {
            def bufferSize = (int) Math.min(readBuffer, count - pos)
            def buffer1 = ByteBuffer.allocate(bufferSize)
            def buffer2 = ByteBuffer.allocate(bufferSize)

            def readCount1 = fileChannel1.read(buffer1, offset + pos).get()
            def readCount2 = fileChannel2.read(buffer2, pos).get()

            if (readCount1 != readCount2 || buffer1 != buffer2) {
                return false
            }

            pos += bufferSize
        }

        def verificationRead = fileChannel2.read(ByteBuffer.allocate(1), pos).get()

        fileChannel1.close()
        fileChannel2.close()

        return pos == count && verificationRead == -1
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
    def setupBlobMatchCondition(BlobClientBase bc, String match) {
        if (match == receivedEtag) {
            return bc.getProperties().getETag()
        } else {
            return match
        }
    }

    def setupBlobMatchCondition(BlobAsyncClientBase bac, String match) {
        if (match == receivedEtag) {
            return bac.getProperties().block().getETag()
        } else {
            return match
        }
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing lease Id. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param bc
     *      The blob on which to acquire a lease.
     * @param leaseID
     *      The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or {@code null}.
     * @return
     * The actual lease Id of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    def setupBlobLeaseCondition(BlobClientBase bc, String leaseID) {
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

    def setupBlobLeaseCondition(BlobAsyncClientBase bac, String leaseID) {
        String responseLeaseId = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            responseLeaseId = new BlobLeaseClientBuilder()
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

    def setupContainerMatchCondition(BlobContainerClient cu, String match) {
        if (match == receivedEtag) {
            return cu.getProperties().getETag()
        } else {
            return match
        }
    }

    def setupContainerLeaseCondition(BlobContainerClient cu, String leaseID) {
        if (leaseID == receivedLeaseID) {
            return createLeaseClient(cu).acquireLease(-1)
        } else {
            return leaseID
        }
    }

    def waitForCopy(BlobContainerClient bu, String status) {
        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = bu.getPropertiesWithResponse(null, null, null).getHeaders().getValue("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleepIfLive(1000)
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
        return headers.getValue("etag") != null &&
            // Quotes should be scrubbed from etag header values
            !headers.getValue("etag").contains("\"") &&
            headers.getValue("last-modified") != null &&
            headers.getValue("x-ms-request-id") != null &&
            headers.getValue("x-ms-version") != null &&
            headers.getValue("date") != null
    }

    def validateBlobProperties(Response<BlobProperties> response, String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, byte[] contentMD5, String contentType) {
        return response.getValue().getCacheControl() == cacheControl &&
            response.getValue().getContentDisposition() == contentDisposition &&
            response.getValue().getContentEncoding() == contentEncoding &&
            response.getValue().getContentLanguage() == contentLanguage &&
            response.getValue().getContentMd5() == contentMD5 &&
            response.getValue().getContentType() == contentType
    }

    def enableSoftDelete() {
        primaryBlobServiceClient.setProperties(new BlobServiceProperties()
            .setDeleteRetentionPolicy(new BlobRetentionPolicy().setEnabled(true).setDays(2)))

        sleepIfLive(30000)
    }

    def disableSoftDelete() {
        primaryBlobServiceClient.setProperties(new BlobServiceProperties()
            .setDeleteRetentionPolicy(new BlobRetentionPolicy().setEnabled(false)))

        sleepIfLive(30000)
    }

    class MockRetryRangeResponsePolicy implements HttpPipelinePolicy {
        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().flatMap { HttpResponse response ->
                if (response.getRequest().getHeaders().getValue("x-ms-range") != "bytes=2-6") {
                    return Mono.<HttpResponse>error(new IllegalArgumentException("The range header was not set correctly on retry."))
                } else {
                    // ETag can be a dummy value. It's not validated, but DownloadResponse requires one
                    return Mono.just(new StorageMockHttpResponse(response, 206, Flux.error(new IOException())))
                }
            }
        }
    }
}
