package com.azure.storage.blob.specialized.cryptography

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.implementation.Constants
import spock.lang.Requires
import spock.lang.Shared

class EncryptedBlobOutputStreamTest extends APISpec {

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
        fakeKey = new FakeKey(keyId, getRandomByteArray(256))
        fakeKeyResolver = new FakeKeyResolver(fakeKey)

        cc = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildClient()
            .getBlobContainerClient(generateContainerName())
        cc.create()

        def blobName = generateBlobName()

        beac = getEncryptedClientBuilder(fakeKey, null, primaryCredential,
            cc.getBlobContainerUrl())
            .blobName(blobName)
            .buildEncryptedBlobAsyncClient()

        bec = getEncryptedClientBuilder(fakeKey, null, primaryCredential,
            cc.getBlobContainerUrl().toString())
            .blobName(blobName)
            .buildEncryptedBlobClient()
    }

    @Requires({ liveMode() })
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

    @Requires({ liveMode() })
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

    @Requires({ liveMode() })
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

    @Requires({ liveMode() })
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

    @Requires({ liveMode() })
    def "Encrypted blob output stream overwrite"() {
        setup:
        def data = getRandomByteArray(10 * Constants.MB)
        beac.upload(defaultFlux, null)

        when:
        def outputStream = bec.getBlobOutputStream(true)
        outputStream.write(data)
        outputStream.close()

        then:
        convertInputStreamToByteArray(bec.openInputStream()) == data
    }

    def convertInputStreamToByteArray(InputStream inputStream) {
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
