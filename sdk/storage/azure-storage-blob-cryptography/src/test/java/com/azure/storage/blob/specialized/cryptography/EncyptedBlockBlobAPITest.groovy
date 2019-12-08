package com.azure.storage.blob.specialized.cryptography

import com.azure.core.cryptography.AsyncKeyEncryptionKey
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.specialized.BlockBlobAsyncClient
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.common.implementation.Constants
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.BlobEncryptionPolicy
import com.microsoft.azure.storage.blob.BlobRequestOptions
import com.microsoft.azure.storage.blob.CloudBlobClient
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.blob.CloudBlockBlob
import reactor.core.publisher.Flux
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll

import java.nio.ByteBuffer

class EncyptedBlockBlobAPITest extends APISpec {

    EncryptedBlobClient bec // encrypted client
    EncryptedBlobAsyncClient beac // encrypted async client
    BlobContainerClient cc

    String keyId

    @Shared
    def fakeKey

    @Shared
    def fakeKeyResolver


    def setup() {
        keyId = "keyId"
        fakeKey = new FakeKey(keyId, 256)
        fakeKeyResolver = new FakeKeyResolver(fakeKey)

        cc = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildClient()
            .getBlobContainerClient(generateContainerName())
        cc.create()

        beac = getEncryptedClientBuilder(fakeKey, null, primaryCredential,
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient()

        bec = getEncryptedClientBuilder(fakeKey, null, primaryCredential,
            cc.getBlobContainerUrl().toString())
            .blobName(generateBlobName())
            .buildEncryptedBlobClient()
    }

    def cleanup() {
        cc.delete()
    }

    // Key and key resolver null
    @Requires({ liveMode() })
    def "Create encryption client fails"() {
        when:
        beac = getEncryptedClientBuilder(null, null, primaryCredential,
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient()

        then:
        thrown(IllegalArgumentException)

        when:
        bec = getEncryptedClientBuilder(null, null, primaryCredential,
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobClient()

        then:
        thrown(IllegalArgumentException)
    }

    // Check that all valid ways to specify the key and keyResolver work
    @Unroll
    @Requires({ liveMode() })
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
        beac = getEncryptedClientBuilder(key, keyResolver, primaryCredential,
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient()

        then:
        notThrown(IllegalArgumentException)

        when:
        bec = getEncryptedClientBuilder(key, keyResolver, primaryCredential,
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

    @Requires({ liveMode() })
    def "Test Async and Sync EncryptedBlockBlobClient to BlockBlobClient"() {
        when:
        beac.upload(defaultFlux, null).block()
        BlockBlobAsyncClient normalAsyncClient = beac.getBlockBlobAsyncClient()

        then:
        normalAsyncClient.blobUrl == beac.blobUrl
        normalAsyncClient.getHttpPipeline().policyCount == beac.getHttpPipeline().policyCount
        normalAsyncClient.getHttpPipeline().httpClient == beac.getHttpPipeline().httpClient

        and:
        normalAsyncClient.download().blockLast()

        then:
        notThrown(BlobStorageException)

        when:
        bec.uploadFromFile(getRandomFile(KB).toPath().toString())
        BlockBlobClient normalClient = bec.getBlockBlobClient()

        // Check that an encrypted client has correct number of policies and important properties are the same
        then:
        normalClient.blobUrl == bec.blobUrl
        normalClient.getHttpPipeline().policyCount == bec.getHttpPipeline().policyCount
        normalClient.getHttpPipeline().httpClient == bec.getHttpPipeline().httpClient

        and:
        normalClient.download(new ByteArrayOutputStream())

        then:
        notThrown(BlobStorageException)

    }

    // This test checks that encryption is not just a no-op
    @Requires({ liveMode() })
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
    @Requires({ liveMode() })
    def "Encryption"() {
        when:
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
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(size, 2, null)
        beac.upload(flux, parallelTransferOptions).block()
        ByteBuffer outputByteBuffer = collectBytesInBuffer(beac.download()).block()

        then:
        compareListToBuffer(byteBufferList, outputByteBuffer)

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
        5 * Constants.KB  | Constants.KB      // 9 Large number of small buffers.
        10 * Constants.MB | 2                 // 10 Small number of large buffers.
    }

    // This test checks that HTTP headers are successfully set on the encrypted client
    @Unroll
    @Requires({ liveMode() })
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
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(defaultDataSize, 2, null)
        beac.uploadWithResponse(defaultFlux, parallelTransferOptions, headers, null, null, null).block()
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
    @Requires({ liveMode() })
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
        beac.uploadWithResponse(defaultFlux, null, null, metadata, null, null).block()
        def properties = beac.getProperties().block()

        then:
        properties.getMetadata() == metadata

        when:
        // Buffered upload
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(defaultDataSize as int, 2, null)
        beac.uploadWithResponse(defaultFlux, parallelTransferOptions, null, metadata, null, null).block()
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
    @Requires({ liveMode() })
    def "Encryption AC"() {
        when:
        beac.upload(defaultFlux, null).block()
        def etag = setupBlobMatchCondition(beac, match)
        leaseID = setupBlobLeaseCondition(beac, leaseID)
        BlobRequestConditions bac = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(etag)
            .setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        then:
        beac.uploadWithResponse(defaultFlux, null, null, null, null, bac).block().getStatusCode() == 201

        when:
        etag = setupBlobMatchCondition(beac, match)
        bac.setIfMatch(etag)

        then:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(defaultDataSize as int, 2, null)
        beac.uploadWithResponse(defaultFlux, parallelTransferOptions, null, null, null, bac)
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
    @Requires({ liveMode() })
    def "Encryption AC fail"() {
        setup:
        beac.upload(defaultFlux, null).block()
        noneMatch = setupBlobMatchCondition(beac, noneMatch)
        setupBlobLeaseCondition(beac, leaseID)
        BlobRequestConditions bac = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(defaultDataSize, 2, null)
        beac.uploadWithResponse(defaultFlux, parallelTransferOptions, null, null, null, bac).block()

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
    @Requires({ liveMode() })
    def "Encrypted upload file"() {
        setup:
        def file = getRandomFile(KB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()

        then:
        compareDataToFile(beac.download(), file)
    }

    // This test checks the download to file method on an encrypted client
    @Requires({ liveMode() })
    def "Encrypted download file"() {
        setup:
        def path = UUID.randomUUID().toString() + ".txt"
        //def dataFlux = Flux.just(defaultData).map{buf -> buf.duplicate()}

        when:
        beac.upload(defaultFlux, null).block()
        beac.downloadToFile(path).block()

        then:
        compareDataToFile(defaultFlux, new File(path))

        cleanup:
        new File(path).delete()
    }

    @Requires({ liveMode() })
    def "Download unencrypted data"() {
        setup:
        // Create an async client
        BlobContainerClient cac = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildClient()
            .getBlobContainerClient(generateContainerName())

        cac.create()
        def blobName = generateBlobName()

        BlockBlobClient normalClient = cac.getBlobClient(blobName).getBlockBlobClient()

        // Uses builder method that takes in regular blob clients
        EncryptedBlobClient client = getEncryptedClientBuilder(fakeKey as AsyncKeyEncryptionKey, null,
            primaryCredential, cac.getBlobContainerUrl())
            .blobName(blobName)
            .buildEncryptedBlobClient()

        when:

        // Upload encrypted data with regular client
        normalClient.uploadWithResponse(new ByteArrayInputStream(defaultData.array()), defaultDataSize, null, null,
            null, null, null, null, null)

        // Download data with encrypted client - command should fail
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        client.download(os)

        then:
        notThrown(IllegalStateException)
        os.toByteArray() == defaultData.array()

        cleanup:
        cac.delete()
    }

    // Tests key resolver
    @Unroll
    @Requires({ liveMode() })
    def "Key resolver used to decrypt data"() {
        setup:
        def blobName = generateBlobName()

        EncryptedBlobAsyncClient decryptResolverClient =
            getEncryptedClientBuilder(null, fakeKeyResolver as AsyncKeyEncryptionKeyResolver, primaryCredential,
                cc.getBlobContainerUrl())
                .blobName(blobName)
                .buildEncryptedBlobAsyncClient()

        EncryptedBlobAsyncClient encryptClient =
            getEncryptedClientBuilder(fakeKey as AsyncKeyEncryptionKey, null, primaryCredential, cc.getBlobContainerUrl())
                .blobName(blobName)
                .buildEncryptedBlobAsyncClient()
        when:
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
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(size, 2, null)
        encryptClient.upload(flux, parallelTransferOptions).block()
        ByteBuffer outputByteBuffer = collectBytesInBuffer(decryptResolverClient.download()).block()

        then:
        compareListToBuffer(byteBufferList, outputByteBuffer)

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
        5 * Constants.KB  | Constants.KB      // 9 Large number of small buffers.
        10 * Constants.MB | 2                 // 10 Small number of large buffers.
    }

    // Upload with old SDK download with new SDk.
    @Requires({ liveMode() })
    def "Cross platform test upload old download new"() {
        setup:
        def blobName = generateBlobName()
        def containerName = cc.getBlobContainerName()

        CloudStorageAccount v8Account = CloudStorageAccount.parse(connectionString)
        CloudBlobClient blobClient = v8Account.createCloudBlobClient()
        CloudBlobContainer container = blobClient.getContainerReference(containerName)
        CloudBlockBlob v8EncryptBlob = container.getBlockBlobReference(blobName)
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(fakeKey, null)
        BlobRequestOptions uploadOptions = new BlobRequestOptions()
        uploadOptions.setEncryptionPolicy(uploadPolicy)
        ByteArrayInputStream inputStream = new ByteArrayInputStream(defaultData.array())

        EncryptedBlobClient decryptClient =
            getEncryptedClientBuilder(fakeKey as AsyncKeyEncryptionKey, null, primaryCredential, cc.getBlobContainerUrl())
                .blobName(blobName)
                .buildEncryptedBlobClient()

        when:
        // Upload with v8


        v8EncryptBlob.upload(inputStream, defaultDataSize, null, uploadOptions, null)

        // Download with current version

        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        decryptClient.download(stream)

        then:
        stream.toByteArray() == defaultData.array()
    }

    // Upload with new SDK download with old SDk.
    @Requires({ liveMode() })
    def "Cross platform test upload new download old"() {
        setup:
        def blobName = generateBlobName()
        def containerName = cc.getBlobContainerName()

        EncryptedBlobAsyncClient encryptClient =
            getEncryptedClientBuilder(fakeKey as AsyncKeyEncryptionKey, null, primaryCredential,
                cc.getBlobContainerUrl())
                .blobName(blobName)
                .buildEncryptedBlobAsyncClient()

        CloudStorageAccount v8Account = CloudStorageAccount.parse(connectionString)
        CloudBlobClient blobClient = v8Account.createCloudBlobClient()
        CloudBlobContainer container = blobClient.getContainerReference(containerName)
        CloudBlockBlob v8DecryptBlob = container.getBlockBlobReference(blobName)
        BlobEncryptionPolicy policy = new BlobEncryptionPolicy(fakeKey, null)
        BlobRequestOptions downloadOptions = new BlobRequestOptions()
        downloadOptions.setEncryptionPolicy(policy)

        when:
        // Upload with current version
        encryptClient.upload(defaultFlux, null).block()

        // Download with v8
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        v8DecryptBlob.download(stream, null, downloadOptions, null)

        then:
        stream.toByteArray() == defaultData.array()
    }

    @Requires({ liveMode() })
    def "encrypted client file upload overwrite false"() {
        setup:
        def file = getRandomFile(KB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()

        beac.uploadFromFile(file.toPath().toString()).block()

        then:
        thrown(IllegalArgumentException)
    }

    @Requires({ liveMode() })
    def "encrypted client file upload overwrite true"() {
        setup:
        def file = getRandomFile(KB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()
        beac.uploadFromFile(file.toPath().toString(), true).block()

        then:
        notThrown(Throwable)
    }

    @Requires({ liveMode() })
    def "encrypted client upload overwrite false"() {
        setup:
        ByteBuffer byteBuffer = getRandomData(Constants.KB)

        when:
        beac.upload(Flux.just(byteBuffer), null).block()

        beac.upload(Flux.just(byteBuffer), null).block()

        then:
        thrown(IllegalArgumentException)
    }

    @Requires({ liveMode() })
    def "encrypted client upload overwrite true"() {
        setup:
        ByteBuffer byteBuffer = getRandomData(Constants.KB)

        when:
        beac.upload(Flux.just(byteBuffer), null).block()

        beac.upload(Flux.just(byteBuffer), null, true).block()

        then:
        notThrown(Throwable)
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
}
