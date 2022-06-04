// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.storage.common.implementation.Constants
import com.azure.storage.file.datalake.models.CustomerProvidedKey
import com.azure.storage.file.datalake.models.DataLakeStorageException

class CPKTest extends APISpec {
    CustomerProvidedKey key
    DataLakeFileClient cpkFile
    DataLakeDirectoryClient cpkDirectory
    DataLakeFileSystemClient cpkFileSystem

    def setup() {
        key = new CustomerProvidedKey(getRandomKey())
        def builder = instrument(new DataLakeFileSystemClientBuilder())
            .endpoint(fsc.getFileSystemUrl().toString())
            .customerProvidedKey(key)
            .credential(environment.dataLakeAccount.credential)

        cpkFileSystem = builder.buildClient()
        cpkDirectory = cpkFileSystem.getDirectoryClient(generatePathName())
        cpkFile = cpkFileSystem.getFileClient(generatePathName())
    }

    /**
     * Insecurely and quickly generates a random AES256 key for the purpose of unit tests. No one should ever make a
     * real key this way.
     */
    def getRandomKey(long seed = new Random().nextLong()) {
        def key = new byte[32] // 256 bit key
        new Random(seed).nextBytes(key)
        return key
    }

    def "Path create"() {
        when:
        def response = cpkDirectory.createWithResponse(null, null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionKeySha256() == key.getKeySha256()
    }

    def "Path get properties"() {
        setup:
        cpkFile.create()

        when:
        def response = cpkFile.getPropertiesWithResponse(null, null, null)

        then:
        response.getStatusCode() == 200
        Boolean.parseBoolean(response.getHeaders().getValue(Constants.HeaderConstants.SERVER_ENCRYPTED))
        response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySha256()
    }

    def "Path set metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")
        cpkFile.create()

        when:
        def response = cpkFile.setMetadataWithResponse(metadata, null, null, null)

        then:
        response.getStatusCode() == 200
        Boolean.parseBoolean(response.getHeaders().getValue(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
        response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySha256()
    }

    def "File read"() {
        setup:
        cpkFile.create()

        when:
        cpkFile.readWithResponse(new ByteArrayOutputStream(), null, null, null, false, null, null)

        then:
        notThrown(DataLakeStorageException)
    }

    def "File append"() {
        setup:
        cpkFile.create()

        when:
        def response = cpkFile.appendWithResponse(getData().defaultInputStream, 0, getData().defaultDataSizeLong, null,
            null, null, null)

        then:
        Boolean.parseBoolean(response.getHeaders().getValue(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
        response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySha256()
    }

    def "File flush"() {
        setup:
        cpkFile.create()
        cpkFile.append(getData().defaultInputStream, 0, getData().defaultDataSizeLong)

        when:
        def response = cpkFile.flushWithResponse(getData().defaultDataSizeLong, true, true, null, null, null, null)

        then:
        Boolean.parseBoolean(response.getHeaders().getValue(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
        response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySha256()
    }

    def "Directory createSupDir"() {
        setup:
        cpkDirectory.create()

        when:
        def response = cpkDirectory.createSubdirectoryWithResponse(generatePathName(), null, null, null, null, null, null,
            null)

        then:
        response.getValue().getCustomerProvidedKey().getKeySha256() == key.getKeySha256()
        Boolean.parseBoolean(response.getHeaders().getValue(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
        response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySha256()
    }

    def "getCustomerProvidedKeyClient"() {
        setup:
        def newCpk = new CustomerProvidedKey(getRandomKey())

        when: "FileClieng"
        def newCpkFileClient = cpkFile.getCustomerProvidedKeyClient(newCpk)

        then:
        newCpkFileClient instanceof DataLakeFileClient
        newCpkFileClient.getCustomerProvidedKey() != cpkFile.getCustomerProvidedKey()

        when: "Directory"
        def newCpkDirectoryClient = cpkDirectory.getCustomerProvidedKeyClient(newCpk)

        then:
        newCpkDirectoryClient instanceof DataLakeDirectoryClient
        newCpkDirectoryClient.getCustomerProvidedKey() != cpkDirectory.getCustomerProvidedKey()

        when: "PathClient"
        def newCpkPathClient = ((DataLakePathClient) cpkFile).getCustomerProvidedKeyClient(newCpk)

        then:
        newCpkPathClient instanceof DataLakePathClient
        newCpkPathClient.getCustomerProvidedKey() != cpkFile.getCustomerProvidedKey()
    }

    def "Exists without CPK"() {
        setup:
        cpkFile.create()
        def clientWithoutCpk = cpkFile.getCustomerProvidedKeyClient(null)

        expect:
        clientWithoutCpk.exists()
    }
}
