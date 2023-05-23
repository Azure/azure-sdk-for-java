package com.azure.storage.blob.specialized.cryptography

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly

class EncryptedBlobOutputStreamTest extends APISpec {

    EncryptedBlobClient bec // encrypted client
    EncryptedBlobAsyncClient beac // encrypted async client
    BlobContainerClient cc

    String keyId
    def fakeKey
    def fakeKeyResolver


    def setup() {
        keyId = "keyId"
        fakeKey = new FakeKey(keyId, getRandomByteArray(256))
        fakeKeyResolver = new FakeKeyResolver(fakeKey)

        cc = getServiceClientBuilder(environment.primaryAccount)
            .buildClient()
            .getBlobContainerClient(generateContainerName())
        cc.create()

        def blobName = generateBlobName()

        beac = getEncryptedClientBuilder(fakeKey, null, environment.primaryAccount.credential,
            cc.getBlobContainerUrl())
            .blobName(blobName)
            .buildEncryptedBlobAsyncClient()

        bec = getEncryptedClientBuilder(fakeKey, null, environment.primaryAccount.credential,
            cc.getBlobContainerUrl().toString())
            .blobName(blobName)
            .buildEncryptedBlobClient()
    }

    @LiveOnly
    def "Encrypted blob output stream not a no op"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)
        def os = new ByteArrayOutputStream()

        when:
        def outputStream = bec.getBlobOutputStream()
        outputStream.write(data)
        outputStream.close()

        cc.getBlobClient(bec.getBlobName()).download(os)

        then:
        os.toByteArray() != data
    }

    @LiveOnly
    def "Encrypted blob output stream"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)

        when:
        def outputStream = bec.getBlobOutputStream()
        outputStream.write(data)
        outputStream.close()

        then:
        convertInputStreamToByteArray(bec.openInputStream()) == data
    }

    @LiveOnly
    def "Encrypted blob output stream default no overwrite"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)

        when:
        def outputStream1 = bec.getBlobOutputStream()
        outputStream1.write(data)
        outputStream1.close()

        and:
        bec.getBlobOutputStream()

        then:
        thrown(IllegalArgumentException)
    }

    @LiveOnly
    def "Encrypted blob output stream default no overwrite interrupted"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)

        when:
        def outputStream1 = bec.getBlobOutputStream()
        def outputStream2 = bec.getBlobOutputStream()
        outputStream2.write(data)
        outputStream2.close()

        and:
        outputStream1.write(data)
        outputStream1.close()

        then:
        def e = thrown(IOException)
        e.getCause() instanceof BlobStorageException
        ((BlobStorageException) e.getCause()).getErrorCode() == BlobErrorCode.BLOB_ALREADY_EXISTS
    }

    @LiveOnly
    def "Encrypted blob output stream overwrite"() {
        setup:
        def randomData = getRandomByteArray(10 * Constants.MB)
        beac.upload(data.defaultFlux, null)

        when:
        def outputStream = bec.getBlobOutputStream(true)
        outputStream.write(randomData)
        outputStream.close()

        then:
        convertInputStreamToByteArray(bec.openInputStream()) == randomData
    }

    static def convertInputStreamToByteArray(InputStream inputStream) {
        int b
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }

        return outputStream.toByteArray()
    }
}
