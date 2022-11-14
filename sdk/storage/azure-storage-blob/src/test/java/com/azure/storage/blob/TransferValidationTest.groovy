package com.azure.storage.blob

import com.azure.storage.blob.options.BlockBlobOutputStreamOptions
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions
import com.azure.storage.blob.options.BlockBlobStageBlockOptions
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.StorageChecksumAlgorithm
import com.azure.storage.common.UploadTransferValidationOptions
import com.azure.storage.common.implementation.ChecksumUtils
import com.azure.storage.common.test.shared.policy.RequestAssertionPolicy
import spock.lang.Unroll

class TransferValidationTest extends APISpec {
    @Unroll
    def "Stage block with checksum"() {
        setup:
        def expectedChecksumBase64 = ChecksumUtils.checksumDataAsync(data.defaultBinaryData, algorithm).block()
            .getT2().getChecksum().encodeBase64().toString()
        def blobName = generateBlobName()
        def bbClient = new SpecializedBlobClientBuilder().blobClient(getBlobClient(
            environment.primaryAccount.credential, cc.getBlobClient(blobName).getBlobUrl(),
            RequestAssertionPolicy.getHeaderEqualsAssertionPolicy(checksumHeader, expectedChecksumBase64))
        ).buildBlockBlobClient()

        when:
        bbClient.stageBlockWithResponse(new BlockBlobStageBlockOptions("myid".bytes.encodeBase64().toString(), data.defaultBinaryData)
            .setTransferValidation(new UploadTransferValidationOptions().setChecksumAlgorithm(algorithm)), null, null)

        then:
        notThrown(Exception)

        where:
        algorithm                             | checksumHeader
        StorageChecksumAlgorithm.Auto         | "x-ms-content-crc64"
        StorageChecksumAlgorithm.StorageCrc64 | "x-ms-content-crc64"
        StorageChecksumAlgorithm.MD5          | "Content-MD5"
    }

    @Unroll
    def "Stage block no checksum"() {
        setup:
        def blobName = generateBlobName()
        def bbClient = new SpecializedBlobClientBuilder().blobClient(getBlobClient(
            environment.primaryAccount.credential, cc.getBlobClient(blobName).getBlobUrl(),
            RequestAssertionPolicy.getHeaderNotExistsAssertionPolicy("x-ms-content-crc64"),
            RequestAssertionPolicy.getHeaderNotExistsAssertionPolicy("Content-MD5"))
        ).buildBlockBlobClient()

        when:
        bbClient.stageBlockWithResponse(new BlockBlobStageBlockOptions("myid".bytes.encodeBase64().toString(), data.defaultBinaryData)
            .setTransferValidation(validationOptions), null, null)

        then:
        notThrown(Exception)

        where:
        _ || validationOptions
        _ || null
        _ || new UploadTransferValidationOptions().setChecksumAlgorithm(StorageChecksumAlgorithm.None)
    }

    @Unroll
    def "Simple upload with checksum"() {
        setup:
        def expectedChecksumBase64 = ChecksumUtils.checksumDataAsync(data.defaultBinaryData, algorithm).block()
            .getT2().getChecksum().encodeBase64().toString()
        def blobName = generateBlobName()
        def bbClient = new SpecializedBlobClientBuilder().blobClient(getBlobClient(
            environment.primaryAccount.credential, cc.getBlobClient(blobName).getBlobUrl(),
            RequestAssertionPolicy.getHeaderEqualsAssertionPolicy(checksumHeader, expectedChecksumBase64))
        ).buildBlockBlobClient()

        when:
        bbClient.uploadWithResponse(new BlockBlobSimpleUploadOptions(data.defaultBinaryData)
            .setTransferValidation(new UploadTransferValidationOptions().setChecksumAlgorithm(algorithm)), null, null)

        then:
        notThrown(Exception)

        where:
        algorithm                             | checksumHeader
        StorageChecksumAlgorithm.Auto         | "x-ms-content-crc64"
        StorageChecksumAlgorithm.StorageCrc64 | "x-ms-content-crc64"
        StorageChecksumAlgorithm.MD5          | "Content-MD5"
    }

    @Unroll
    def "Simple upload no checksum"() {
        setup:
        def blobName = generateBlobName()
        def bbClient = new SpecializedBlobClientBuilder().blobClient(getBlobClient(
            environment.primaryAccount.credential, cc.getBlobClient(blobName).getBlobUrl(),
            RequestAssertionPolicy.getHeaderNotExistsAssertionPolicy("x-ms-content-crc64"),
            RequestAssertionPolicy.getHeaderNotExistsAssertionPolicy("Content-MD5"))
        ).buildBlockBlobClient()

        when:
        bbClient.uploadWithResponse(new BlockBlobSimpleUploadOptions(data.defaultBinaryData)
            .setTransferValidation(validationOptions), null, null)

        then:
        notThrown(Exception)

        where:
        _ || validationOptions
        _ || null
        _ || new UploadTransferValidationOptions().setChecksumAlgorithm(StorageChecksumAlgorithm.None)
    }

    @Unroll
    def "Block blob output stream with checksum"() {
        setup:
        def expectedChecksumBase64 = ChecksumUtils.checksumDataAsync(data.defaultBinaryData, algorithm).block()
            .getT2().getChecksum().encodeBase64().toString()
        def blobName = generateBlobName()
        def bbClient = new SpecializedBlobClientBuilder().blobClient(getBlobClient(
            environment.primaryAccount.credential, cc.getBlobClient(blobName).getBlobUrl(),
            RequestAssertionPolicy.getHeaderExistsAssertionPolicy(checksumHeader,
                // TODO (jaschrep): revert to method reference operator, following #31504 merge
                { RequestAssertionPolicy.isStageBlock(it) }))
        ).buildBlockBlobClient()

        when:
        def stream = bbClient.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setTransferValidation(new UploadTransferValidationOptions().setChecksumAlgorithm(algorithm)))
        stream.write(data.defaultBytes)
        stream.flush()
        stream.close()

        then:
        notThrown(Exception)

        where:
        algorithm                             | checksumHeader
        StorageChecksumAlgorithm.Auto         | "x-ms-content-crc64"
        StorageChecksumAlgorithm.StorageCrc64 | "x-ms-content-crc64"
        StorageChecksumAlgorithm.MD5          | "Content-MD5"
    }

    @Unroll
    def "Block blob output stream no checksum"() {
        setup:
        def blobName = generateBlobName()
        def bbClient = new SpecializedBlobClientBuilder().blobClient(getBlobClient(
            environment.primaryAccount.credential, cc.getBlobClient(blobName).getBlobUrl(),
            RequestAssertionPolicy.getHeaderNotExistsAssertionPolicy("x-ms-content-crc64"),
            RequestAssertionPolicy.getHeaderNotExistsAssertionPolicy("Content-MD5"))
        ).buildBlockBlobClient()

        when:
        def stream = bbClient.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setTransferValidation(validationOptions))
        stream.write(data.defaultBytes)
        stream.flush()
        stream.close()

        then:
        notThrown(Exception)

        where:
        _ || validationOptions
        _ || null
        _ || new UploadTransferValidationOptions().setChecksumAlgorithm(StorageChecksumAlgorithm.None)
    }
}
