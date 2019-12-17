package com.azure.storage.file.datalake

import com.azure.storage.file.datalake.models.DataLakeStorageException
import reactor.test.StepVerifier

class AsyncErrorMappingTest extends APISpec {

    def "Read file"() {
        when:
        def fileName = generatePathName()
        def fac = fscAsync.getFileAsyncClient(fileName)
        def readFileVerifier = StepVerifier.create(fac.readWithResponse(null, null, null, false))
        then:
        readFileVerifier.verifyError(DataLakeStorageException)
    }

    def "Get file properties"() {
        when:
        def fileName = generatePathName()
        def fac = fscAsync.getFileAsyncClient(fileName)
        def getPropertiesVerifier = StepVerifier.create(fac.getPropertiesWithResponse(null))
        then:
        getPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set file http properties"() {
        when:
        def fileName = generatePathName()
        def fac = fscAsync.getFileAsyncClient(fileName)
        def setPropertiesVerifier = StepVerifier.create(fac.setHttpHeadersWithResponse(null, null))
        then:
        setPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set file metadata"() {
        when:
        def fileName = generatePathName()
        def fac = fscAsync.getFileAsyncClient(fileName)
        def setMetadataVerifier = StepVerifier.create(fac.setMetadataWithResponse(null, null))
        then:
        setMetadataVerifier.verifyError(DataLakeStorageException)
    }

    def "Get directory properties"() {
        when:
        def directoryName = generatePathName()
        def dac = fscAsync.getDirectoryAsyncClient(directoryName)
        def getPropertiesVerifier = StepVerifier.create(dac.getPropertiesWithResponse(null))
        then:
        getPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set directory http properties"() {
        when:
        def directoryName = generatePathName()
        def dac = fscAsync.getDirectoryAsyncClient(directoryName)
        def setPropertiesVerifier = StepVerifier.create(dac.setHttpHeadersWithResponse(null, null))
        then:
        setPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set directory metadata"() {
        when:
        def directoryName = generatePathName()
        def dac = fscAsync.getDirectoryAsyncClient(directoryName)
        def setMetadataVerifier = StepVerifier.create(dac.setMetadataWithResponse(null, null))
        then:
        setMetadataVerifier.verifyError(DataLakeStorageException)
    }

    def "Create file system"() {
        when:
        def createVerifier = StepVerifier.create(fscAsync.createWithResponse(null, null))
        then:
        createVerifier.verifyError(DataLakeStorageException)
    }

    def "Get file system properties"() {
        when:
        def asyncFileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(primaryCredential).getFileSystemAsyncClient(asyncFileSystemName)
        def getPropertiesVerifier = StepVerifier.create(fsac.getPropertiesWithResponse(null))
        then:
        getPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set file system metadata"() {
        when:
        def asyncFileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(primaryCredential).getFileSystemAsyncClient(asyncFileSystemName)
        def setMetadataVerifier = StepVerifier.create(fsac.setMetadataWithResponse(null, null))
        then:
        setMetadataVerifier.verifyError(DataLakeStorageException)
    }

    def "Delete file system"() {
        when:
        def asyncFileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(primaryCredential).getFileSystemAsyncClient(asyncFileSystemName)
        def deleteFileVerifier = StepVerifier.create(fsac.deleteWithResponse(null))
        then:
        deleteFileVerifier.verifyError(DataLakeStorageException)
    }

    def "Get file system access policy"() {
        when:
        def asyncFileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(primaryCredential).getFileSystemAsyncClient(asyncFileSystemName)
        def getAccessPolicyVerifier = StepVerifier.create(fsac.getAccessPolicyWithResponse(null))
        then:
        getAccessPolicyVerifier.verifyError(DataLakeStorageException)
    }

    def "Set file system access policy"() {
        when:
        def asyncFileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(primaryCredential).getFileSystemAsyncClient(asyncFileSystemName)
        def setAccessPolicyVerifier = StepVerifier.create(fsac.setAccessPolicyWithResponse(null, null, null))
        then:
        setAccessPolicyVerifier.verifyError(DataLakeStorageException)
    }

}
