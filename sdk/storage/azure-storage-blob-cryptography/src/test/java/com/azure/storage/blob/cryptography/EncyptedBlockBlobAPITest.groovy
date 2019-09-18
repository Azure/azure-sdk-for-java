package com.azure.storage.blob.cryptography


import com.azure.storage.blob.BlockBlobAsyncClient
import com.azure.storage.blob.ContainerAsyncClient
import com.azure.storage.blob.ContainerClient
import com.azure.storage.blob.URLParser
import com.azure.storage.blob.models.BlobAccessConditions
import com.azure.storage.blob.models.BlobHTTPHeaders
import com.azure.storage.blob.models.LeaseAccessConditions
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.ModifiedAccessConditions
import com.azure.storage.blob.models.StorageErrorCode
import com.azure.storage.blob.models.StorageException
import com.azure.storage.common.Constants
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import reactor.core.publisher.Flux
import spock.lang.Ignore
import spock.lang.Unroll

import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest

class EncyptedBlockBlobAPITest extends APISpec {

    EncryptedBlockBlobClient bec // encrypted client
    EncryptedBlockBlobAsyncClient beac // encrypted async client
    ContainerClient cc

    String keyId
    def symmetricKey

    def setup() {
        keyId = "keyId"
        KeyGenerator keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        SecretKey secretKey = keyGen.generateKey()
        symmetricKey = new SymmetricKey(keyId, secretKey.getEncoded())
        cc = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildClient()
            .getContainerClient(generateContainerName())
        cc.create()

        beac = getEncryptedClientBuilder(symmetricKey as IKey, null, primaryCredential,
            cc.getContainerUrl().toString())
            .blobName(generateBlobName())
            .buildEncryptedBlockBlobAsyncClient()

        bec = getEncryptedClientBuilder(symmetricKey as IKey, null, primaryCredential,
            cc.getContainerUrl().toString())
            .blobName(generateBlobName())
            .buildEncryptedBlockBlobClient()
    }

    // Key or key resolver must be set
    def "Create encryption client fails"() {
        when:
        beac = getEncryptedClientBuilder(null, null, primaryCredential,
            cc.getContainerUrl().toString())
            .blobName(generateBlobName())
            .buildEncryptedBlockBlobAsyncClient()

        then:
        thrown(NullPointerException)

        when:
        bec = getEncryptedClientBuilder(null, null, primaryCredential,
            cc.getContainerUrl().toString())
            .blobName(generateBlobName())
            .buildEncryptedBlockBlobClient()

        then:
        thrown(NullPointerException)
    }

    // This test checks that encryption is not just a no-op
    def "Encryption not a no-op"() {
        setup:
        ByteBuffer byteBuffer = getRandomData(Constants.KB)
        def is = new ByteArrayInputStream(byteBuffer.array())
        def os = new ByteArrayOutputStream()

        when:
        bec.upload(is, Constants.KB)
        cc.getBlobClient(URLParser.parse(bec.getBlobUrl()).getBlobName()).download(os)

        ByteBuffer outputByteBuffer = ByteBuffer.wrap(os.toByteArray())

        then:
        outputByteBuffer.array() != byteBuffer.array()
    }

    // This test uses an encrypted client to encrypt and decrypt data
    // Tests upload and buffered upload with different bytebuffer sizes
    @Unroll
    def "Encryption"() {
        when:
        def byteBufferList = [];

        /*
        Sending a sequence of buffers allows us to test encryption behavior in different cases when the buffers do
        or do not align on encryption boundaries.
         */
        for (def i = 0; i < byteBufferCount; i++) {
            byteBufferList.add(getRandomData(size))
        }
        Flux<ByteBuffer> flux = Flux.fromIterable(byteBufferList)

        // Test basic upload.
        beac.upload(flux, size * byteBufferCount).block()
        ByteBuffer outputByteBuffer = collectBytesInBuffer(beac.download().block()).block()

        then:
        compareListToBuffer(byteBufferList, outputByteBuffer)

        when:
        // Test buffered upload.
        beac.upload(flux, size, 2).block()
        outputByteBuffer = collectBytesInBuffer(beac.download().block()).block()

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
    def "Encryption HTTP headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders().setBlobCacheControl(cacheControl)
            .setBlobContentDisposition(contentDisposition)
            .setBlobContentEncoding(contentEncoding)
            .setBlobContentLanguage(contentLanguage)
            .setBlobContentMD5(contentMD5)
            .setBlobContentType(contentType)

        /* TODO : (gapra) Find out how exactly to fix this, this fails because the contentMd5 being sent to the service
            is that of unencrypted data */
//        when:
//        beac.uploadWithResponse(defaultFlux, defaultDataSize, headers, null, null, null).block()
//        def response = beac.getPropertiesWithResponse(null).block()
//
//        then:
//        response.getStatusCode() == 200
//
//        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage,
//            contentMD5, contentType == null ? "application/octet-stream" : contentType, contentMD5 != null)
//        // HTTP default content type is application/octet-stream

        when:
        // Buffered upload
        beac.uploadWithResponse(defaultFlux, defaultDataSize as int, 2, headers, null, null, null).block()
        def response = beac.getPropertiesWithResponse(null).block()

        then:
        response.getStatusCode() == 200
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage,
            contentMD5, contentType == null ? "application/octet-stream" : contentType, contentMD5 != null)
        // HTTP default content type is application/octet-stream

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        null         | null               | null            | null            | null                                                         | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    // This test checks that metadata in encryption is successfully set
    @Unroll
    def "Encryption metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        beac.uploadWithResponse(defaultFlux, defaultDataSize, null, metadata, null, null).block()
        def properties = beac.getProperties().block()

        then:
        properties.getMetadata() == metadata

        when:
        // Buffered upload
        beac.uploadWithResponse(defaultFlux, defaultDataSize as int, 2, null, metadata, null, null).block()
        properties = beac.getProperties().block()

        then:
        properties.getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    // This test checks that access conditions in encryption clients is successfully set
    // TODO: (gapra) Blocked on making LeaseClientBuilder a full fledged builder
    @Ignore
    @Unroll
    def "Encryption AC"() {
        when:
        beac.upload(defaultFlux, defaultDataSize).block()
        def etag = setupBlobMatchCondition(beac, match)
        leaseID = setupBlobLeaseCondition(beac, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().setModifiedAccessConditions(
            new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)
                .setIfMatch(etag).setIfNoneMatch(noneMatch))
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))

        then:
        beac.uploadWithResponse(defaultFlux, defaultDataSize, null, null, null, bac).block().getStatusCode() == 201

        when:
        etag = setupBlobMatchCondition(beac, match)
        bac.getModifiedAccessConditions().setIfMatch(etag)

        then:
        beac.uploadWithResponse(defaultFlux, defaultDataSize as int, 2, null, null, null, bac)
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

    // This test checks that access conditions in encryption clients is unsuccessful with invalid data
    // TODO: (gapra) Blocked on making LeaseClientBuilder a full fledged builder
    @Ignore
    @Unroll
    def "Encryption AC fail"() {
        setup:
        beac.upload(defaultFlux, defaultDataSize).block()
        noneMatch = setupBlobMatchCondition(beac, noneMatch)
        setupBlobLeaseCondition(beac, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().setModifiedAccessConditions(
            new ModifiedAccessConditions().setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)
                .setIfMatch(match).setIfNoneMatch(noneMatch))
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))

        when:
        beac.uploadWithResponse(defaultFlux, defaultDataSize, null, null, null, bac).block()
        beac.uploadWithResponse(defaultFlux, defaultDataSize as int, 2, null, null, null, bac).block()

        then:
        def e = thrown(StorageException)
        e.getErrorCode() == StorageErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    // This test checks the upload to file method on an encrypted client
    def "Encrypted upload file"() {
        setup:
        def file = getRandomFile(KB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()

        then:
        compareDataToFile(beac.download().block(), file)
    }

    // This test checks the download to file method on an encrypted client
    def "Encrypted download file"() {
        setup:
        def path = UUID.randomUUID().toString() + ".txt"
        //def dataFlux = Flux.just(defaultData).map{buf -> buf.duplicate()}

        when:
        beac.upload(defaultFlux, defaultDataSize).block()
        beac.downloadToFile(path).block()

        then:
        compareDataToFile(defaultFlux, new File(path))

        cleanup:
        new File(path).delete()
    }

    // This test checks that encryption is cross platform compatible
    // Encrypted data uploaded from a previous version of the SDK can be downloaded from this SDK successfully
    @Unroll
    def "Block blob cross platform decryption tests"() {
        setup:
        // Create an async client
        ContainerAsyncClient cac = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildAsyncClient()
            .getContainerAsyncClient(generateContainerName())

        cac.create().block()

        List<TestEncryptionBlob> list = getTestData("encryptedBlob.json")
        def key = new SymmetricKey("symmKey1", Base64.getDecoder().decode(list.get(index).getKey()))
        def blobName = generateBlobName()

        BlockBlobAsyncClient normalClient = cac.getBlockBlobAsyncClient(blobName)

//        EncryptedBlockBlobAsyncClient client = getEncryptedClientBuilder(key as IKey, null, primaryCredential,
//            cac.getContainerUrl().toString())
//            .blobName(blobName)
//            .buildEncryptedBlockBlobAsyncClient()
        EncryptedBlockBlobAsyncClient client = new EncryptedBlobClientBuilder()
            .keyAndKeyResolver(key, null)
            .buildEncryptedBlockBlobAsyncClient(normalClient)

        byte[] encryptedBytes = Base64.getDecoder().decode(list.get(index).getEncryptedContent())
        byte[] decryptedBytes = Base64.getDecoder().decode(list.get(index).getDecryptedContent())

        when:
        Metadata metadata = new Metadata()
        ObjectMapper objectMapper = new ObjectMapper()
        metadata.put(EncryptionConstants.ENCRYPTION_DATA_KEY, objectMapper.writeValueAsString(list.get(index).
            getEncryptionData()))

        // Upload encrypted data with regular client
        normalClient.uploadWithResponse(Flux.just(ByteBuffer.wrap(encryptedBytes)), encryptedBytes.length, null,
            metadata, null, null).block()

        // Download data with encrypted client
        ByteBuffer outputByteBuffer = collectBytesInBuffer(client.download().block()).block()

        then:
        outputByteBuffer.array() == decryptedBytes

        where:
        index << [0, 1, 2, 3, 4]
    }

    def downloadUnencryptedData() {
        setup:
        // Create an async client
        ContainerAsyncClient cac = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildAsyncClient()
            .getContainerAsyncClient(generateContainerName())

        cac.create().block()
        def blobName = generateBlobName()

        BlockBlobAsyncClient normalClient = cac.getBlockBlobAsyncClient(blobName)

//        EncryptedBlockBlobAsyncClient client = getEncryptedClientBuilder(key as IKey, null, primaryCredential,
//            cac.getContainerUrl().toString())
//            .blobName(blobName)
//            .buildEncryptedBlockBlobAsyncClient()
        EncryptedBlockBlobAsyncClient client = new EncryptedBlobClientBuilder()
            .keyAndKeyResolver(symmetricKey, null)
            .buildEncryptedBlockBlobAsyncClient(normalClient)

        when:

        // Upload encrypted data with regular client
        normalClient.uploadWithResponse(defaultFlux, defaultDataSize, null,
            null, null, null).block()

        // Download data with encrypted client
        client.download().block()

        then:
        thrown(IllegalStateException)
    }

    def getTestData(String fileName) {
        Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI())
        String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
        ObjectMapper mapper = new ObjectMapper()
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class,
            TestEncryptionBlob.class)
        List<TestEncryptionBlob> list = mapper.readValue(json, collectionType)
        return list
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
