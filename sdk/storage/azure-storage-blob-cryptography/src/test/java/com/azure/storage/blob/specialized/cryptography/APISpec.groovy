package com.azure.storage.blob.specialized.cryptography

import com.azure.core.cryptography.AsyncKeyEncryptionKey
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.http.rest.Response
import com.azure.core.test.TestMode
import com.azure.core.util.FluxUtil
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobClientBuilder
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.models.BlobProperties
import com.azure.storage.blob.specialized.BlobLeaseClient
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.StorageSpec
import com.azure.storage.common.test.shared.TestAccount
import org.mockito.Mockito
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import javax.crypto.SecretKey
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime

class APISpec extends StorageSpec {

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

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedLeaseID = "received"

    static final String garbageLeaseID = UUID.randomUUID().toString()

    static def mockRandomData = String.join("", Collections.nCopies(32, "password")).getBytes(StandardCharsets.UTF_8)

    /*
    Size must be an int because ByteBuffer sizes can only be an int. Long is not supported.
    */
    ByteBuffer getRandomData(int size) {
        return ByteBuffer.wrap(getRandomByteArray(size))
    }

    byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(namer.getRandomUuid()).getMostSignificantBits() & Long.MAX_VALUE
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

    EncryptedBlobClientBuilder getEncryptedClientBuilder(AsyncKeyEncryptionKey key,
                                                         AsyncKeyEncryptionKeyResolver keyResolver,
                                                         StorageSharedKeyCredential credential, String endpoint,
                                                         HttpPipelinePolicy... policies) {

        KeyWrapAlgorithm algorithm = key != null && key.getKeyId().block() == "local" ? KeyWrapAlgorithm.A256KW : KeyWrapAlgorithm.RSA_OAEP_256
        EncryptedBlobClientBuilder builder = new EncryptedBlobClientBuilder()
            .key(key, algorithm.toString())
            .keyResolver(keyResolver)
            .endpoint(endpoint)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        instrument(builder)

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    BlobClientBuilder getBlobClientBuilder(StorageSharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {

        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        instrument(builder)

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    BlobServiceClientBuilder getServiceClientBuilder(TestAccount account,
                                                     HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(account.credential, account.blobEndpoint, policies)
    }

    BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
                                                     HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        instrument(builder)

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    def generateContainerName() {
        generateResourceName(entityNo++)
    }

    private String generateResourceName(int entityNo) {
        return namer.getRandomName(namer.getResourcePrefix() + entityNo, 63)
    }

    def generateBlobName() {
        generateResourceName(entityNo++)
    }

    def validateBlobProperties(Response<BlobProperties> response, String cacheControl, String contentDisposition, String contentEncoding,
                               String contentLanguage, byte[] contentMD5, String contentType, boolean checkContentMD5) {

        def propertiesEqual = response.getValue().getCacheControl() == cacheControl &&
            response.getValue().getContentDisposition() == contentDisposition &&
            response.getValue().getContentEncoding() == contentEncoding &&
            response.getValue().getContentLanguage() == contentLanguage &&
            response.getHeaders().getValue("Content-Type") == contentType

        if (checkContentMD5) {
            return propertiesEqual && response.getValue().getContentMd5() == contentMD5
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
    def setupBlobMatchCondition(EncryptedBlobClient ebbc, String match) {
        if (match == receivedEtag) {
            return ebbc.getPropertiesWithResponse(null, null, null).getHeaders().getValue("ETag")
        } else {
            return match
        }
    }

    def setupBlobMatchCondition(EncryptedBlobAsyncClient ebbac, String match) {
        if (match == receivedEtag) {
            return ebbac.getPropertiesWithResponse(null).block().getHeaders().getValue("ETag")
        } else {
            return match
        }
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing leaseId. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param bc
     *      The blob on which to acquire a lease.
     * @param leaseID
     *      The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or {@code null}.
     * @return
     * The actual leaseId of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
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

    static BlobLeaseClient createLeaseClient(BlobClient blobClient) {
        return createLeaseClient(blobClient, null)
    }

    static BlobLeaseClient createLeaseClient(BlobClient blobClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient()
    }

    def compareDataToFile(Flux<ByteBuffer> data, File file) {
        FileInputStream fis = new FileInputStream(file)

        for (ByteBuffer received : data.toIterable()) {
            byte[] readBuffer = new byte[received.remaining()]
            fis.read(readBuffer)
            for (int i = 0; i < received.remaining(); i++) {
                if (readBuffer[i] != received.get(i)) {
                    return false
                }
            }
        }

        fis.close()
        return true
    }

    class MockRetryRangeResponsePolicy implements HttpPipelinePolicy {
        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().flatMap { HttpResponse response ->
                if (response.getRequest().getHeaders().getValue("x-ms-range") != "bytes=0-15") {
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
        def defaultBufferSize = 128 * Constants.KB
        def stream1 = new FileInputStream(file1)
        stream1.skip(offset)
        def stream2 = new FileInputStream(file2)

        try {
            // If the amount we are going to read is smaller than the default buffer size use that instead.
            def bufferSize = (int) Math.min(defaultBufferSize, count)

            while (pos < count) {
                // Number of bytes we expect to read.
                def expectedReadCount = (int) Math.min(bufferSize, count - pos)
                def buffer1 = new byte[expectedReadCount]
                def buffer2 = new byte[expectedReadCount]

                def readCount1 = stream1.read(buffer1)
                def readCount2 = stream2.read(buffer2)

                // Use Arrays.equals as it is more optimized than Groovy/Spock's '==' for arrays.
                assert readCount1 == readCount2 && Arrays.equals(buffer1, buffer2)

                pos += expectedReadCount
            }

            def verificationRead = stream2.read()
            return pos == count && verificationRead == -1
        } finally {
            stream1.close()
            stream2.close()
        }
    }

    private static def mockKey = String.join("", Collections.nCopies(4, "password")).getBytes(StandardCharsets.UTF_8)

    /**
     * Insecurely and quickly generates a random AES256 key for the purpose of unit tests. No one should ever make a
     * real key this way.
     */
    static def getRandomKey(long seed = new Random().nextLong()) {
        if (getEnv().getTestMode() == TestMode.LIVE) {
            def key = new byte[32] // 256 bit key
            new Random(seed).nextBytes(key)
            return key
        } else {
            return mockKey
        }
    }

    /**
     * Stubs the generateAesKey method in EncryptedBlobAsyncClient to return a consistent SecretKey used in PLAYBACK
     * and RECORD testing modes only.
     */
    static def mockAesKey(EncryptedBlobAsyncClient encryptedClient) {
        if (getEnv().getTestMode() != TestMode.LIVE) {
            def mockAesKey = new SecretKey() {
                @Override
                String getAlgorithm() {
                    return CryptographyConstants.AES
                }

                @Override
                String getFormat() {
                    return "RAW"
                }

                @Override
                byte[] getEncoded() {
                    return mockKey
                }
            }

            encryptedClient = Mockito.spy(encryptedClient)
            Mockito.when(encryptedClient.generateSecretKey()).thenReturn(mockAesKey)
        }

        return encryptedClient
    }
}
