package com.azure.storage.file.datalake

import com.azure.core.http.rest.PagedFlux
import com.azure.storage.file.datalake.models.DataLakeStorageException
import com.azure.storage.file.datalake.models.FileSystemItem
import reactor.test.StepVerifier

class AsyncErrorMappingTest extends APISpec {

    DataLakeFileSystemAsyncClient fsac
    String fileSystemName

    def setup() {
        fileSystemName = generateFileSystemName()
        fsac = getServiceAsyncClient(env.dataLakeAccount).createFileSystem(fileSystemName).block()
    }

    def "Read file"() {
        when:
        def fileName = generatePathName()
        def fac = fsac.getFileAsyncClient(fileName)
        def readFileVerifier = StepVerifier.create(fac.readWithResponse(null, null, null, false))
        then:
        readFileVerifier.verifyError(DataLakeStorageException)
    }

    def "Get file properties"() {
        when:
        def fileName = generatePathName()
        def fac = fsac.getFileAsyncClient(fileName)
        def getPropertiesVerifier = StepVerifier.create(fac.getPropertiesWithResponse(null))
        then:
        getPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set file http properties"() {
        when:
        def fileName = generatePathName()
        def fac = fsac.getFileAsyncClient(fileName)
        def setPropertiesVerifier = StepVerifier.create(fac.setHttpHeadersWithResponse(null, null))
        then:
        setPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set file metadata"() {
        when:
        def fileName = generatePathName()
        def fac = fsac.getFileAsyncClient(fileName)
        def setMetadataVerifier = StepVerifier.create(fac.setMetadataWithResponse(null, null))
        then:
        setMetadataVerifier.verifyError(DataLakeStorageException)
    }

    def "Get directory properties"() {
        when:
        def directoryName = generatePathName()
        def dac = fsac.getDirectoryAsyncClient(directoryName)
        def getPropertiesVerifier = StepVerifier.create(dac.getPropertiesWithResponse(null))
        then:
        getPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set directory http properties"() {
        when:
        def directoryName = generatePathName()
        def dac = fsac.getDirectoryAsyncClient(directoryName)
        def setPropertiesVerifier = StepVerifier.create(dac.setHttpHeadersWithResponse(null, null))
        then:
        setPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set directory metadata"() {
        when:
        def directoryName = generatePathName()
        def dac = fsac.getDirectoryAsyncClient(directoryName)
        def setMetadataVerifier = StepVerifier.create(dac.setMetadataWithResponse(null, null))
        then:
        setMetadataVerifier.verifyError(DataLakeStorageException)
    }

    def "Create file system"() {
        when:
        def fsac = getServiceAsyncClient(env.dataLakeAccount).getFileSystemAsyncClient(fileSystemName)
        def createVerifier = StepVerifier.create(fsac.createWithResponse(null, null))
        then:
        createVerifier.verifyError(DataLakeStorageException)
    }

    def "Get file system properties"() {
        when:
        def fileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(env.dataLakeAccount).getFileSystemAsyncClient(fileSystemName)
        def getPropertiesVerifier = StepVerifier.create(fsac.getPropertiesWithResponse(null))
        then:
        getPropertiesVerifier.verifyError(DataLakeStorageException)
    }

    def "Set file system metadata"() {
        when:
        def fileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(env.dataLakeAccount).getFileSystemAsyncClient(fileSystemName)
        def setMetadataVerifier = StepVerifier.create(fsac.setMetadataWithResponse(null, null))
        then:
        setMetadataVerifier.verifyError(DataLakeStorageException)
    }

    def "Delete file system"() {
        when:
        def fileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(env.dataLakeAccount).getFileSystemAsyncClient(fileSystemName)
        def setMetadataVerifier = StepVerifier.create(fsac.deleteWithResponse(null))
        then:
        setMetadataVerifier.verifyError(DataLakeStorageException)
    }

    def "Get file system access policy"() {
        when:
        def fileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(env.dataLakeAccount).getFileSystemAsyncClient(fileSystemName)
        def getAccessPolicyVerifier = StepVerifier.create(fsac.getAccessPolicyWithResponse(null))
        then:
        getAccessPolicyVerifier.verifyError(DataLakeStorageException)
    }

    def "Set file system access policy"() {
        when:
        def fileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(env.dataLakeAccount).getFileSystemAsyncClient(fileSystemName)
        def setAccessPolicyVerifier = StepVerifier.create(fsac.setAccessPolicyWithResponse(null, null, null))
        then:
        setAccessPolicyVerifier.verifyError(DataLakeStorageException)
    }


    def "List file systems"() {
        when:
        PagedFlux<FileSystemItem> items = getServiceAsyncClient(env.dataLakeAccount).listFileSystems()
        def listFileSystemsVerifier = StepVerifier.create(items.byPage("garbage continuation token").count())
        then:
        listFileSystemsVerifier.verifyError(DataLakeStorageException)
    }

}
