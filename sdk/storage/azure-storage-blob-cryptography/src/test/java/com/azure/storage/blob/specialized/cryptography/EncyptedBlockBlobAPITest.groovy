package com.azure.storage.blob.specialized.cryptography

import com.azure.core.cryptography.AsyncKeyEncryptionKey
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpPipelinePosition
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobClientBuilder
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.ProgressReceiver
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.DownloadRetryOptions
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.LeaseStatusType
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.options.BlobParallelUploadOptions
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.TestHttpClientType
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.BlobEncryptionPolicy
import com.microsoft.azure.storage.blob.BlobRequestOptions
import com.microsoft.azure.storage.blob.CloudBlobClient
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.blob.CloudBlockBlob
import reactor.core.Exceptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class EncyptedBlockBlobAPITest extends APISpec {

    EncryptedBlobClient bec // encrypted client
    EncryptedBlobAsyncClient beac // encrypted async client
    BlobContainerClient cc
    EncryptedBlobClient ebc // encrypted client for download

    String keyId
    FakeKey fakeKey
    def fakeKeyResolver


    def setup() {
        keyId = "keyId"
        fakeKey = new FakeKey(keyId, getRandomByteArray(256))
        fakeKeyResolver = new FakeKeyResolver(fakeKey)

        cc = getServiceClientBuilder(env.primaryAccount)
            .buildClient()
            .getBlobContainerClient(generateContainerName())
        cc.create()

        beac = getEncryptedClientBuilder(fakeKey, null, env.primaryAccount.credential,
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient()

        bec = getEncryptedClientBuilder(fakeKey, null, env.primaryAccount.credential,
            cc.getBlobContainerUrl().toString())
            .blobName(generateBlobName())
            .buildEncryptedBlobClient()

        def blobName = generateBlobName()

        def builder = getEncryptedClientBuilder(fakeKey, null, env.primaryAccount.credential,
            cc.getBlobContainerUrl().toString())
            .blobName(blobName)

        builder.buildEncryptedBlobAsyncClient().upload(data.defaultFlux, null).block()

        ebc = builder.buildEncryptedBlobClient()
    }

    def cleanup() {
        cc.delete()
    }

    // Key and key resolver null
    def "Create encryption client fails"() {
        when:
        beac = getEncryptedClientBuilder(null, null, env.primaryAccount.credential,
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient()

        then:
        thrown(IllegalArgumentException)

        when:
        bec = getEncryptedClientBuilder(null, null, env.primaryAccount.credential,
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobClient()

        then:
        thrown(IllegalArgumentException)
    }

    // Check that all valid ways to specify the key and keyResolver work
    @Unroll
    def "Create encryption client succeeds"() {
        when:
        def key
        if (passKey) {
            key = fakeKey
        } else {
            key = null
        }
        def keyResolver
        if (passKeyResolver) {
            keyResolver = fakeKeyResolver
        } else {
            keyResolver = null
        }
        beac = getEncryptedClientBuilder(key, keyResolver, env.primaryAccount.credential,
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient()

        then:
        notThrown(IllegalArgumentException)

        when:
        bec = getEncryptedClientBuilder(key, keyResolver, env.primaryAccount.credential,
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobClient()

        then:
        notThrown(IllegalArgumentException)

        where:
        passKey | passKeyResolver
        true    | false
        false   | true
        true    | true
    }

    // This test checks that encryption is not just a no-op
    def "Encryption not a no-op"() {
        setup:
        ByteBuffer byteBuffer = getRandomData(Constants.KB)
        def os = new ByteArrayOutputStream()

        when:
        beac.upload(Flux.just(byteBuffer), null).block()
        cc.getBlobClient(beac.getBlobName()).download(os)

        ByteBuffer outputByteBuffer = ByteBuffer.wrap(os.toByteArray())

        then:
        outputByteBuffer.array() != byteBuffer.array()
    }

    // This test uses an encrypted client to encrypt and decrypt data
    // Tests upload and buffered upload with different bytebuffer sizes
    @Unroll
    def "Encryption"() {
        expect:
        encryptionTestHelper(size, byteBufferCount)

        where:
        size              | byteBufferCount
        5                 | 2                 // 0 Two buffers smaller than an encryption block.
        8                 | 2                 // 1 Two buffers that equal an encryption block.
        10                | 1                 // 2 One buffer smaller than an encryption block.
        10                | 2                 // 3 A buffer that spans an encryption block.
        16                | 1                 // 4 A buffer exactly the same size as an encryption block.
        16                | 2                 // 5 Two buffers the same size as an encryption block.
        20                | 1                 // 6 One buffer larger than an encryption block.
        20                | 2                 // 7 Two buffers larger than an encryption block.
        100               | 1                 // 8 One buffer containing multiple encryption blocks
    }

    @Unroll
    @LiveOnly
    def "Encryption large"() {
        expect:
        encryptionTestHelper(size, byteBufferCount)

        where:
        size              | byteBufferCount
        5 * Constants.KB  | Constants.KB      // 9 Large number of small buffers.
        10 * Constants.MB | 2                 // 10 Small number of large buffers.
    }

    boolean encryptionTestHelper(int size, int byteBufferCount) {
        def byteBufferList = []

        /*
        Sending a sequence of buffers allows us to test encryption behavior in different cases when the buffers do
        or do not align on encryption boundaries.
         */
        for (def i = 0; i < byteBufferCount; i++) {
            byteBufferList.add(getRandomData(size))
        }
        Flux<ByteBuffer> flux = Flux.fromIterable(byteBufferList)


        // Test buffered upload.
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(size)
            .setMaxConcurrency(2)
        beac.upload(flux, parallelTransferOptions).block()
        ByteBuffer outputByteBuffer = collectBytesInBuffer(beac.download()).block()

        return compareListToBuffer(byteBufferList, outputByteBuffer)
    }

    @Unroll
    @LiveOnly
    def "Encryption computeMd5"() {
        setup:
        def byteBufferList = []
        for (def i = 0; i < byteBufferCount; i++) {
            byteBufferList.add(getRandomData(size))
        }
        Flux<ByteBuffer> flux = Flux.fromIterable(byteBufferList)
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setMaxSingleUploadSizeLong(maxSingleUploadSize)
            .setBlockSizeLong(blockSize)

        expect:
        beac.uploadWithResponse(new BlobParallelUploadOptions(flux).setParallelTransferOptions(parallelTransferOptions).setComputeMd5(true)).block().getStatusCode() == 201

        where:
        size           | maxSingleUploadSize | blockSize               | byteBufferCount
        Constants.KB   | null                | null                    | 1                  // Simple case where uploadFull is called.
        Constants.KB   | Constants.KB        | 500 * Constants.KB      | 1000               // uploadChunked 2 blocks staged
        Constants.KB   | Constants.KB        | 5 * Constants.KB        | 1000               // uploadChunked 100 blocks staged
    }

    // This test checks that HTTP headers are successfully set on the encrypted client
    @Unroll
    def "Encryption HTTP headers"() {
        setup:
        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        when:
        // Buffered upload
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(data.defaultDataSize).setMaxConcurrency(2)
        beac.uploadWithResponse(data.defaultFlux, parallelTransferOptions, headers, null, null, null).block()
        def response = beac.getPropertiesWithResponse(null).block()

        then:
        response.getStatusCode() == 200
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage,
            contentMD5, contentType == null ? "application/octet-stream" : contentType, contentMD5 != null)
        // HTTP default content type is application/octet-stream

        where:
        // Don't calculate MD5 as we would need to encrypt the blob then calculate it.
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5 | contentType
        null         | null               | null            | null            | null       | null
        "control"    | "disposition"      | "encoding"      | "language"      | null       | "type"
    }

    // This test checks that metadata in encryption is successfully set
    @Unroll
    @LiveOnly
    def "Encryption metadata"() {
        setup:
        Map<String, String> metadata = new HashMap<>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        beac.uploadWithResponse(data.defaultFlux, null, null, metadata, null, null).block()
        def properties = beac.getProperties().block()

        then:
        properties.getMetadata() == metadata

        when:
        // Buffered upload
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(data.defaultDataSize)
            .setMaxConcurrency(2)
        beac.uploadWithResponse(data.defaultFlux, parallelTransferOptions, null, metadata, null, null).block()
        properties = beac.getProperties().block()

        then:
        properties.getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    // This test checks that access conditions in encryption clients are successfully set
    @Unroll
    def "Encryption AC"() {
        when:
        beac.upload(data.defaultFlux, null).block()
        def etag = setupBlobMatchCondition(beac, match)
        leaseID = setupBlobLeaseCondition(beac, leaseID)
        BlobRequestConditions bac = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(etag)
            .setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        then:
        beac.uploadWithResponse(data.defaultFlux, null, null, null, null, bac).block().getStatusCode() == 201

        when:
        etag = setupBlobMatchCondition(beac, match)
        bac.setIfMatch(etag)

        then:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(data.defaultDataSize).setMaxConcurrency(2)
        beac.uploadWithResponse(data.defaultFlux, parallelTransferOptions, null, null, null, bac)
            .block().getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    // This test checks that access conditions in encryption clients are unsuccessful with invalid data
    @Unroll
    def "Encryption AC fail"() {
        setup:
        beac.upload(data.defaultFlux, null).block()
        noneMatch = setupBlobMatchCondition(beac, noneMatch)
        setupBlobLeaseCondition(beac, leaseID)
        BlobRequestConditions bac = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(data.defaultDataSize).setMaxConcurrency(2)
        beac.uploadWithResponse(data.defaultFlux, parallelTransferOptions, null, null, null, bac).block()

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    // This test checks the upload to file method on an encrypted client
    @LiveOnly
    def "Encrypted upload file"() {
        setup:
        def file = getRandomFile(Constants.MB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()

        then:
        compareDataToFile(beac.download(), file)
    }

    // This test checks the download to file method on an encrypted client
    def "Encrypted download file"() {
        setup:
        def path = UUID.randomUUID().toString() + ".txt"
        //def dataFlux = Flux.just(defaultData).map{buf -> buf.duplicate()}

        when:
        beac.upload(data.defaultFlux, null).block()
        beac.downloadToFile(path).block()

        then:
        compareDataToFile(data.defaultFlux, new File(path))

        cleanup:
        new File(path).delete()
    }

    def "Download unencrypted data"() {
        setup:
        // Create an async client
        BlobContainerClient cac = getServiceClientBuilder(env.primaryAccount)
            .buildClient()
            .getBlobContainerClient(generateContainerName())

        cac.create()
        def blobName = generateBlobName()

        BlockBlobClient normalClient = cac.getBlobClient(blobName).getBlockBlobClient()

        // Uses builder method that takes in regular blob clients
        EncryptedBlobClient client = getEncryptedClientBuilder(fakeKey as AsyncKeyEncryptionKey, null,
            env.primaryAccount.credential, cac.getBlobContainerUrl())
            .blobName(blobName)
            .buildEncryptedBlobClient()

        when:

        // Upload encrypted data with regular client
        normalClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null,
            null, null, null, null, null)

        // Download data with encrypted client - command should fail
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        client.download(os)

        then:
        notThrown(IllegalStateException)
        os.toByteArray() == data.defaultBytes

        cleanup:
        cac.delete()
    }

    // Tests key resolver
    @Unroll
    def "Key resolver used to decrypt data"() {
        expect:
        keyResolverTestHelper(size, byteBufferCount)

        where:
        size              | byteBufferCount
        5                 | 2                 // 0 Two buffers smaller than an encryption block.
        8                 | 2                 // 1 Two buffers that equal an encryption block.
        10                | 1                 // 2 One buffer smaller than an encryption block.
        10                | 2                 // 3 A buffer that spans an encryption block.
        16                | 1                 // 4 A buffer exactly the same size as an encryption block.
        16                | 2                 // 5 Two buffers the same size as an encryption block.
        20                | 1                 // 6 One buffer larger than an encryption block.
        20                | 2                 // 7 Two buffers larger than an encryption block.
        100               | 1                 // 8 One buffer containing multiple encryption blocks
    }

    // Tests key resolver
    @Unroll
    @LiveOnly
    def "Key resolver used to decrypt data large"() {
        expect:
        keyResolverTestHelper(size, byteBufferCount)

        where:
        size              | byteBufferCount
        5 * Constants.KB  | Constants.KB      // 9 Large number of small buffers.
        10 * Constants.MB | 2                 // 10 Small number of large buffers.
    }

    boolean keyResolverTestHelper(int size, int byteBufferCount) {
        def blobName = generateBlobName()

        EncryptedBlobAsyncClient decryptResolverClient =
            getEncryptedClientBuilder(null, fakeKeyResolver as AsyncKeyEncryptionKeyResolver, env.primaryAccount.credential,
                cc.getBlobContainerUrl())
                .blobName(blobName)
                .buildEncryptedBlobAsyncClient()

        EncryptedBlobAsyncClient encryptClient =
            getEncryptedClientBuilder(fakeKey as AsyncKeyEncryptionKey, null, env.primaryAccount.credential, cc.getBlobContainerUrl())
                .blobName(blobName)
                .buildEncryptedBlobAsyncClient()

        def byteBufferList = []

        /*
        Sending a sequence of buffers allows us to test encryption behavior in different cases when the buffers do
        or do not align on encryption boundaries.
         */
        for (def i = 0; i < byteBufferCount; i++) {
            byteBufferList.add(getRandomData(size))
        }
        Flux<ByteBuffer> flux = Flux.fromIterable(byteBufferList)

        // Test buffered upload.
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(size).setMaxConcurrency(2)
        encryptClient.upload(flux, parallelTransferOptions).block()
        ByteBuffer outputByteBuffer = collectBytesInBuffer(decryptResolverClient.download()).block()

        return compareListToBuffer(byteBufferList, outputByteBuffer)
    }

    // Upload with old SDK download with new SDK.
    @LiveOnly
    def "Cross platform test upload old download new"() {
        setup:
        def blobName = generateBlobName()
        def containerName = cc.getBlobContainerName()

        CloudStorageAccount v8Account = CloudStorageAccount.parse(env.primaryAccount.connectionString)
        CloudBlobClient blobClient = v8Account.createCloudBlobClient()
        CloudBlobContainer container = blobClient.getContainerReference(containerName)
        CloudBlockBlob v8EncryptBlob = container.getBlockBlobReference(blobName)
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(fakeKey, null)
        BlobRequestOptions uploadOptions = new BlobRequestOptions()
        uploadOptions.setEncryptionPolicy(uploadPolicy)

        EncryptedBlobClient decryptClient =
            getEncryptedClientBuilder(fakeKey as AsyncKeyEncryptionKey, null, env.primaryAccount.credential, cc.getBlobContainerUrl())
                .blobName(blobName)
                .buildEncryptedBlobClient()

        def streamSize = Constants.KB + 1
        def data = getRandomByteArray(streamSize)
        def inputStream = new ByteArrayInputStream(data)
        when:
        // Upload with v8
        v8EncryptBlob.upload(inputStream, streamSize, null, uploadOptions, null)

        // Download with current version
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        decryptClient.download(outputStream)

        then:
        outputStream.toByteArray() == data
    }

    // Upload with new SDK download with old SDK.
    @LiveOnly
    def "Cross platform test upload new download old"() {
        setup:
        def blobName = generateBlobName()
        def containerName = cc.getBlobContainerName()

        EncryptedBlobAsyncClient encryptClient =
            getEncryptedClientBuilder(fakeKey as AsyncKeyEncryptionKey, null, env.primaryAccount.credential,
                cc.getBlobContainerUrl())
                .blobName(blobName)
                .buildEncryptedBlobAsyncClient()

        CloudStorageAccount v8Account = CloudStorageAccount.parse(env.primaryAccount.connectionString)
        CloudBlobClient blobClient = v8Account.createCloudBlobClient()
        CloudBlobContainer container = blobClient.getContainerReference(containerName)
        container.createIfNotExists()
        CloudBlockBlob v8DecryptBlob = container.getBlockBlobReference(blobName)
        BlobEncryptionPolicy policy = new BlobEncryptionPolicy(fakeKey, null)
        BlobRequestOptions downloadOptions = new BlobRequestOptions()
        downloadOptions.setEncryptionPolicy(policy)

        when:
        // Upload with current version
        encryptClient.upload(data.defaultFlux, null).block()

        // Download with v8
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        v8DecryptBlob.download(stream, null, downloadOptions, null)

        then:
        stream.toByteArray() == data.defaultBytes
    }

    def "encrypted client file upload overwrite false"() {
        setup:
        def file = getRandomFile(Constants.KB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()

        beac.uploadFromFile(file.toPath().toString()).block()

        then:
        thrown(IllegalArgumentException)
    }

    def "encrypted client file upload overwrite true"() {
        setup:
        def file = getRandomFile(Constants.KB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()
        beac.uploadFromFile(file.toPath().toString(), true).block()

        then:
        notThrown(Throwable)
    }

    def "encrypted client upload overwrite false"() {
        setup:
        ByteBuffer byteBuffer = getRandomData(Constants.KB)

        when:
        beac.upload(Flux.just(byteBuffer), null).block()

        beac.upload(Flux.just(byteBuffer), null).block()

        then:
        thrown(IllegalArgumentException)
    }

    def "encrypted client upload overwrite true"() {
        setup:
        ByteBuffer byteBuffer = getRandomData(Constants.KB)

        when:
        beac.upload(Flux.just(byteBuffer), null).block()

        beac.upload(Flux.just(byteBuffer), null, true).block()

        then:
        notThrown(Throwable)
    }

    def "Buffered upload nonMarkableStream"() {
        setup:
        def file = getRandomFile(10)
        def fileStream = new FileInputStream(file)
        def outFile = getRandomFile(10)

        when:
        bec.upload(fileStream, file.size(), true)

        then:
        bec.downloadToFile(outFile.toPath().toString(), true)
        compareFiles(file, outFile, 0, file.size())
    }

    def "Builder bearer token validation"() {
        setup:
        String endpoint = BlobUrlParts.parse(beac.getBlobUrl()).setScheme("http").toUrl()
        def builder = new EncryptedBlobClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildEncryptedBlobClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Download all null"() {
        when:
        def stream = new ByteArrayOutputStream()
        def response = ebc.downloadWithResponse(stream, null, null, null, false, null, null)
        def body = ByteBuffer.wrap(stream.toByteArray())
        def headers = response.getDeserializedHeaders()

        then:
        body == data.defaultData
        headers.getMetadata()
        headers.getContentLength() != null
        headers.getContentType() != null
        headers.getContentRange() == null
        headers.getContentMd5() != null
        headers.getContentEncoding() == null
        headers.getCacheControl() == null
        headers.getContentDisposition() == null
        headers.getContentLanguage() == null
        headers.getBlobSequenceNumber() == null
        headers.getBlobType() == BlobType.BLOCK_BLOB
        headers.getCopyCompletionTime() == null
        headers.getCopyStatusDescription() == null
        headers.getCopyId() == null
        headers.getCopyProgress() == null
        headers.getCopySource() == null
        headers.getCopyStatus() == null
        headers.getLeaseDuration() == null
        headers.getLeaseState() == LeaseStateType.AVAILABLE
        headers.getLeaseStatus() == LeaseStatusType.UNLOCKED
        headers.getAcceptRanges() == "bytes"
        headers.getBlobCommittedBlockCount() == null
        headers.isServerEncrypted() != null
        headers.getBlobContentMD5() == null
    }

    /*
    This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    HttpGetterInfo.
     */

    def "Download with retry range"() {
        /*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the DownloadRetryOptions. The next request should have the same range header, which was generated
        from the count and offset values in HttpGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HttpGetterInfo to have an incorrect offset when it was
        constructed in BlobClient.download().
         */
        setup:
        def builder = getEncryptedClientBuilder(fakeKey, null, env.primaryAccount.credential,
            ebc.getBlobUrl(), new MockRetryRangeResponsePolicy())

        ebc = builder.buildEncryptedBlobClient()

        when:
        def range = new BlobRange(2, 5L)
        def options = new DownloadRetryOptions().setMaxRetryRequests(3)
        ebc.downloadWithResponse(new ByteArrayOutputStream(), range, options, null, false, null, null)

        then:
        /*
        Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         */
        def e = thrown(RuntimeException)
        e.getCause() instanceof IOException
    }

    def "Download min"() {
        when:
        def outStream = new ByteArrayOutputStream()
        ebc.download(outStream)
        def result = outStream.toByteArray()

        then:
        result == data.defaultBytes
    }

    @Unroll
    def "Download range"() {
        setup:
        def range = (count == null) ? new BlobRange(offset) : new BlobRange(offset, count)

        when:
        def outStream = new ByteArrayOutputStream()
        ebc.downloadWithResponse(outStream, range, null, null, false, null, null)
        String bodyStr = outStream.toString()

        then:
        bodyStr == expectedData

        where:
        offset | count || expectedData
        0      | null  || data.defaultText
        0      | 5L    || data.defaultText.substring(0, 5)
        3      | 2L    || data.defaultText.substring(3, 3 + 2)
    }

    @Unroll
    def "Download AC"() {
        setup:
        match = setupBlobMatchCondition(ebc, match)
        leaseID = setupBlobLeaseCondition(ebc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        def response = ebc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null)

        then:
        response.getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Download AC fail"() {
        setup:
        setupBlobLeaseCondition(ebc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(ebc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        ebc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null).getStatusCode()

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Download error"() {
        setup:
        ebc = new EncryptedBlobClientBuilder()
            .key(fakeKey, "keyWrapAlgorithm")
            .blobClient(cc.getBlobClient(generateBlobName()))
            .buildEncryptedBlobClient()

        when:
        ebc.download(null)

        then:
        thrown(NullPointerException)
    }

    def "Download snapshot"() {
        when:
        def originalStream = new ByteArrayOutputStream()
        ebc.download(originalStream)

        def bc2 = ebc.createSnapshot()
        ebc.upload(new ByteArrayInputStream("ABC".getBytes()), "ABC".getBytes().length, true)

        then:
        def snapshotStream = new ByteArrayOutputStream()
        bc2.download(snapshotStream)
        snapshotStream.toByteArray() == originalStream.toByteArray()
    }

    def "Download to file exists"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        // Default overwrite is false so this should fail
        ebc.downloadToFile(testFile.getPath())

        then:
        def ex = thrown(UncheckedIOException)
        ex.getCause() instanceof FileAlreadyExistsException

        cleanup:
        testFile.delete()
    }

    def "Download to file exists succeeds"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        ebc.downloadToFile(testFile.getPath(), true)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == data.defaultText

        cleanup:
        testFile.delete()
    }

    def "Download to file does not exist"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (testFile.exists()) {
            assert testFile.delete()
        }

        when:
        ebc.downloadToFile(testFile.getPath())

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == data.defaultText

        cleanup:
        testFile.delete()
    }

    def "Download file does not exist open options"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (testFile.exists()) {
            assert testFile.delete()
        }

        when:
        Set<OpenOption> openOptions = new HashSet<>()
        openOptions.add(StandardOpenOption.CREATE_NEW)
        openOptions.add(StandardOpenOption.READ)
        openOptions.add(StandardOpenOption.WRITE)
        ebc.downloadToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == data.defaultText

        cleanup:
        testFile.delete()
    }

    def "Download file exist open options"() {
        setup:
        def testFile = new File(namer.getResourcePrefix() + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        Set<OpenOption> openOptions = new HashSet<>()
        openOptions.add(StandardOpenOption.CREATE)
        openOptions.add(StandardOpenOption.TRUNCATE_EXISTING)
        openOptions.add(StandardOpenOption.READ)
        openOptions.add(StandardOpenOption.WRITE)
        ebc.downloadToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == data.defaultText

        cleanup:
        testFile.delete()
    }

    @LiveOnly
    @Unroll
    def "Download file"() {
        setup:
        def file = getRandomFile(fileSize)
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getRandomName(60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def properties = ebc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4 * 1024 * 1024), null, null, false, null, null)

        then:
        compareFiles(file, outFile, 0, fileSize)
        properties.getValue().getBlobType() == BlobType.BLOCK_BLOB

        cleanup:
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        0                    | _ // empty file
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
        // Files larger than 2GB to test no integer overflow are left to stress/perf tests to keep test passes short.
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */
    @LiveOnly
    @Unroll
    def "Download file sync buffer copy"() {
        setup:
        def containerName = generateContainerName()
        def blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(env.primaryAccount.blobEndpoint)
            .credential(env.primaryAccount.credential)
            .buildClient()

        def encryptedBlobClient = new EncryptedBlobClientBuilder()
            .key(fakeKey, "keyWrapAlgorithm")
            .blobClient(blobServiceClient.createBlobContainer(containerName).getBlobClient(generateBlobName()))
            .buildEncryptedBlobClient()

        def file = getRandomFile(fileSize)
        encryptedBlobClient.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getRandomName(60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def properties = encryptedBlobClient.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4 * 1024 * 1024), null, null, false, null, null)

        then:
        compareFiles(file, outFile, 0, fileSize)
        properties.getValue().getBlobType() == BlobType.BLOCK_BLOB

        cleanup:
        blobServiceClient.deleteBlobContainer(containerName)
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        0                    | _ // empty file
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */
    @LiveOnly
    @Unroll
    def "Download file async buffer copy"() {
        setup:
        def containerName = generateContainerName()
        def blobServiceAsyncClient = new BlobServiceClientBuilder()
            .endpoint(env.primaryAccount.blobEndpoint)
            .credential(env.primaryAccount.credential)
            .buildAsyncClient()

        def encryptedBlobAsyncClient = new EncryptedBlobClientBuilder()
            .key(fakeKey, "keyWrapAlgorithm")
            .blobAsyncClient(blobServiceAsyncClient.createBlobContainer(containerName).block()
                .getBlobAsyncClient(generateBlobName()))
            .buildEncryptedBlobAsyncClient()

        def file = getRandomFile(fileSize)
        encryptedBlobAsyncClient.uploadFromFile(file.toPath().toString(), true).block()
        def outFile = new File(namer.getRandomName(60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def downloadMono = encryptedBlobAsyncClient.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4 * 1024 * 1024), null, null, false)

        then:
        StepVerifier.create(downloadMono)
            .assertNext({ it -> it.getValue().getBlobType() == BlobType.BLOCK_BLOB })
            .verifyComplete()

        compareFiles(file, outFile, 0, fileSize)

        cleanup:
        blobServiceAsyncClient.deleteBlobContainer(containerName)
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        0                    | _ // empty file
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
    }

    @LiveOnly
    @Unroll
    def "Download file range"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getRandomName(60))
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), range, null, null, null, false, null, null)

        then:
        compareFiles(file, outFile, range.getOffset(), range.getCount())

        cleanup:
        outFile.delete()
        file.delete()

        /*
        The last case is to test a range much much larger than the size of the file to ensure we don't accidentally
        send off parallel requests with invalid ranges.
         */
        where:
        range                                              | _
        new BlobRange(0, data.defaultDataSize)             | _ // Exact count
        new BlobRange(1, data.defaultDataSize - 1 as Long) | _ // Offset and exact count
        new BlobRange(3, 2)                                | _ // Narrow range in middle
        new BlobRange(0, data.defaultDataSize - 1 as Long) | _ // Count that is less than total
        new BlobRange(0, 10 * 1024)                        | _ // Count much larger than remaining data
    }

    /*
    This is to exercise some additional corner cases and ensure there are no arithmetic errors that give false success.
     */

    @LiveOnly
    @Unroll
    def "Download file range fail"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(34), null, null, null, false,
            null, null)

        then:
        thrown(BlobStorageException)

        cleanup:
        outFile.delete()
        file.delete()
    }

    @LiveOnly
    def "Download file count null"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(0), null, null, null, false, null, null)

        then:
        compareFiles(file, outFile, 0, data.defaultDataSize)

        cleanup:
        outFile.delete()
        file.delete()
    }

    @LiveOnly
    @Unroll
    def "Download file AC"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        match = setupBlobMatchCondition(ebc, match)
        leaseID = setupBlobLeaseCondition(ebc, leaseID)
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null)

        then:
        notThrown(BlobStorageException)

        cleanup:
        outFile.delete()
        file.delete()

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @LiveOnly
    @Unroll
    def "Download file AC fail"() {
        setup:
        def file = getRandomFile(data.defaultDataSize)
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        noneMatch = setupBlobMatchCondition(ebc, noneMatch)
        setupBlobLeaseCondition(ebc, leaseID)
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        cleanup:
        outFile.delete()
        file.delete()

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    @LiveOnly
    def "Download file etag lock"() {
        setup:
        def file = getRandomFile(Constants.MB)
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        Files.deleteIfExists(file.toPath())

        def counter = new AtomicInteger()

        expect:
        def bacUploading = getEncryptedClientBuilder(fakeKey, null, env.primaryAccount.credential,
            ebc.getBlobUrl().toString())
            .buildEncryptedBlobAsyncClient()

        def bacDownloading = getEncryptedClientBuilder(fakeKey, null, env.primaryAccount.credential,
            ebc.getBlobUrl().toString())
            .addPolicy({ context, next ->
                return next.process()
                    .flatMap({ r ->
                        if (counter.incrementAndGet() == 1) {
                            return bacUploading.upload(data.defaultFlux, null, true)
                                .thenReturn(r)
                        }
                        return Mono.just(r)
                    })
            })
            .buildEncryptedBlobAsyncClient()

        /*
         * Setup the download to happen in small chunks so many requests need to be sent, this will give the upload time
         * to change the ETag therefore failing the download.
         */
        def options = new ParallelTransferOptions().setBlockSizeLong(Constants.KB)

        /*
         * This is done to prevent onErrorDropped exceptions from being logged at the error level. If no hook is
         * registered for onErrorDropped the error is logged at the ERROR level.
         *
         * onErrorDropped is triggered once the reactive stream has emitted one element, after that exceptions are
         * dropped.
         */
        Hooks.onErrorDropped({ ignored -> /* do nothing with it */ })

        /*
         * When the download begins trigger an upload to overwrite the downloading blob after waiting 500 milliseconds
         * so that the download is able to get an ETag before it is changed.
         */
        StepVerifier.create(bacDownloading.downloadToFileWithResponse(outFile.toPath().toString(), null, options, null, null, false))
            .verifyErrorSatisfies({
                /*
                 * If an operation is running on multiple threads and multiple return an exception Reactor will combine
                 * them into a CompositeException which needs to be unwrapped. If there is only a single exception
                 * 'Exceptions.unwrapMultiple' will return a singleton list of the exception it was passed.
                 *
                 * These exceptions may be wrapped exceptions where the exception we are expecting is contained within
                 * ReactiveException that needs to be unwrapped. If the passed exception isn't a 'ReactiveException' it
                 * will be returned unmodified by 'Exceptions.unwrap'.
                 */
                assert Exceptions.unwrapMultiple(it).stream().anyMatch({ it2 ->
                    def exception = Exceptions.unwrap(it2)
                    if (exception instanceof BlobStorageException) {
                        assert ((BlobStorageException) exception).getStatusCode() == 412
                        return true
                    }
                })
            })

        // Give the file a chance to be deleted by the download operation before verifying its deletion
        sleep(500)
        !outFile.exists()

        cleanup:
        file.delete()
        outFile.delete()
    }

    @LiveOnly
    @Unroll
    def "Download file progress receiver"() {
        def file = getRandomFile(fileSize)
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(namer.getResourcePrefix())
        if (outFile.exists()) {
            assert outFile.delete()
        }

        def mockReceiver = Mock(ProgressReceiver)

        def numBlocks = fileSize / (4 * 1024 * 1024)
        def prevCount = 0

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setProgressReceiver(mockReceiver),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false, null, null)

        then:
        /*
         * Should receive at least one notification indicating completed progress, multiple notifications may be
         * received if there are empty buffers in the stream.
         */
        (1.._) * mockReceiver.reportProgress(fileSize)

        // There should be NO notification with a larger than expected size.
        0 * mockReceiver.reportProgress({ it > fileSize })

        /*
        We should receive at least one notification reporting an intermediary value per block, but possibly more
        notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
        will be the total size as above. Finally, we assert that the number reported monotonically increases.
         */
        (numBlocks - 1.._) * mockReceiver.reportProgress(!file.size()) >> { long bytesTransferred ->
            if (!(bytesTransferred >= prevCount)) {
                throw new IllegalArgumentException("Reported progress should monotonically increase")
            } else {
                prevCount = bytesTransferred
            }
        }

        // We should receive no notifications that report more progress than the size of the file.
        0 * mockReceiver.reportProgress({ it > fileSize })

        cleanup:
        file.delete()
        outFile.delete()

        where:
        fileSize             | _
        100                  | _
        8 * 1026 * 1024 + 10 | _
    }

    def "Download requiresEncryption"() {
        setup:
        def blobName = bec.getBlobName()
        def bc = getBlobClientBuilder(env.primaryAccount.credential, cc.getBlobContainerUrl().toString())
            .blobName(blobName)
            .buildClient()

        bc.upload(data.defaultInputStream, data.defaultDataSize)

        when: "Sync min"
        bec = getEncryptedClientBuilder(fakeKey, null, env.primaryAccount.credential,
            cc.getBlobContainerUrl().toString())
            .blobName(blobName)
            .requiresEncryption(true)
            .buildEncryptedBlobClient()
        bec.download(new ByteArrayOutputStream())

        then:
        thrown(IllegalStateException)

        when: "Sync max"
        bec.downloadWithResponse(new ByteArrayOutputStream(), null, null, null, false, null, null)

        then:
        thrown(IllegalStateException)

        when: "Async min"
        beac = getEncryptedClientBuilder(fakeKey, null, env.primaryAccount.credential,
            cc.getBlobContainerUrl().toString())
            .blobName(blobName)
            .requiresEncryption(true)
            .buildEncryptedBlobAsyncClient()
        beac.download().blockLast()

        then:
        thrown(IllegalStateException)

        when: "Async max"
        beac.downloadWithResponse(null, null, null, false).block()

        then:
        thrown(IllegalStateException)
    }

    def "Encryption upload IS overwrite fails"() {
        when:
        ebc.upload(data.defaultInputStream, data.defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Encryption upload IS overwrite"() {
        setup:
        def randomData = getRandomByteArray(Constants.KB)
        def input = new ByteArrayInputStream(randomData)

        when:
        ebc.upload(input, Constants.KB, true)

        then:
        def stream = new ByteArrayOutputStream()
        ebc.downloadWithResponse(stream, null, null, null, false, null, null)
        stream.toByteArray() == randomData
    }

    // This test checks that encryption is not just a no-op
    def "Encryption upload IS sync not a no-op"() {
        setup:
        ByteBuffer byteBuffer = getRandomData(Constants.KB)
        def os = new ByteArrayOutputStream()

        when:
        ebc.upload(new ByteArrayInputStream(byteBuffer.array()), byteBuffer.remaining(), true)
        cc.getBlobClient(ebc.getBlobName()).download(os)

        ByteBuffer outputByteBuffer = ByteBuffer.wrap(os.toByteArray())

        then:
        outputByteBuffer.array() != byteBuffer.array()
    }

    @LiveOnly
    def "Encryption upload IS large data"() {
        setup:
        def randomData = getRandomByteArray(20 * Constants.MB)
        def os = new ByteArrayOutputStream()
        def input = new ByteArrayInputStream(randomData)

        def pto = new ParallelTransferOptions().setMaxSingleUploadSizeLong(Constants.MB)

        when:
        // Uses blob output stream under the hood.
        ebc.uploadWithResponse(input, 20 * Constants.MB, pto, null, null, null, null, null, null)
        ebc.download(os)

        then:
        notThrown(BlobStorageException)
        os.toByteArray() == randomData
    }

    @Unroll
    @LiveOnly
    def "Encryption uploadIS numBlocks"() {
        setup:
        def randomData = getRandomByteArray(size)
        def input = new ByteArrayInputStream(randomData)

        def pto = new ParallelTransferOptions().setBlockSizeLong(maxUploadSize as Long).setMaxSingleUploadSizeLong(maxUploadSize as Long)

        when:
        bec.uploadWithResponse(input, size, pto, null, null, null, null, null, null)

        then:
        def blocksUploaded = cc.getBlobClient(bec.getBlobName()).getBlockBlobClient()
            .listBlocks(BlockListType.ALL).getCommittedBlocks()
        blocksUploaded.size() == (int) numBlocks

        where:
        size            | maxUploadSize || numBlocks
        0               | null          || 0
        Constants.KB    | null          || 0 // default is MAX_UPLOAD_BYTES
        Constants.MB    | null          || 0 // default is MAX_UPLOAD_BYTES
        3 * Constants.MB| Constants.MB  || 4 // Encryption padding will add an extra block
    }

    def getPerCallVersionPolicy() {
        return new HttpPipelinePolicy() {
            @Override
            Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                context.getHttpRequest().setHeader("x-ms-version","2017-11-09")
                return next.process()
            }
            @Override
            HttpPipelinePosition getPipelinePosition() {
                return HttpPipelinePosition.PER_CALL
            }
        }
    }

    @IgnoreIf( { getEnv().serviceVersion != null } )
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        def client = getEncryptedClientBuilder(fakeKey, fakeKeyResolver, env.primaryAccount.credential, bec.getBlobUrl(), getPerCallVersionPolicy()).buildEncryptedBlobClient()

        client.upload(new ByteArrayInputStream(new byte[0]), 0)

        when:
        def response = client.getPropertiesWithResponse(null, null, null)

        then:
        notThrown(BlobStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }

    def compareListToBuffer(List<ByteBuffer> buffers, ByteBuffer result) {
        result.position(0)
        for (ByteBuffer buffer : buffers) {
            buffer.position(0)
            result.limit(result.position() + buffer.remaining())
            if (buffer != result) {
                return false
            }
            result.position(result.position() + buffer.remaining())
        }
        return result.remaining() == 0
    }
}
