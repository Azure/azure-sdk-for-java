package com.azure.storage.file.datalake

import com.azure.storage.file.datalake.models.DataLakeStorageException
import reactor.test.StepVerifier

class AsyncErrorMappingTest extends APISpec {

    DataLakeFileSystemAsyncClient fsac
    String fileSystemName

    def setup() {
        fileSystemName = generateFileSystemName()
        fsac = getServiceAsyncClient(primaryCredential).createFileSystem(fileSystemName).block()
    }

    def "Read file"() {
        when:
        def fileName = generatePathName()
        def fac = fsac.getFileAsyncClient(fileName)
        def readFileVerifier = StepVerifier.create(fac.readWithResponse(null, null, null, false))
        then:
        readFileVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Get file properties"() {
        when:
        def fileName = generatePathName()
        def fac = fsac.getFileAsyncClient(fileName)
        def getPropertiesVerifier = StepVerifier.create(fac.getPropertiesWithResponse(null))
        then:
        getPropertiesVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Set file http properties"() {
        when:
        def fileName = generatePathName()
        def fac = fsac.getFileAsyncClient(fileName)
        def setPropertiesVerifier = StepVerifier.create(fac.setHttpHeadersWithResponse(null, null))
        then:
        setPropertiesVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Set file metadata"() {
        when:
        def fileName = generatePathName()
        def fac = fsac.getFileAsyncClient(fileName)
        def setMetadataVerifier = StepVerifier.create(fac.setMetadataWithResponse(null, null))
        then:
        setMetadataVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Get directory properties"() {
        when:
        def directoryName = generatePathName()
        def dac = fsac.getDirectoryAsyncClient(directoryName)
        def getPropertiesVerifier = StepVerifier.create(dac.getPropertiesWithResponse(null))
        then:
        getPropertiesVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Set directory http properties"() {
        when:
        def directoryName = generatePathName()
        def dac = fsac.getDirectoryAsyncClient(directoryName)
        def setPropertiesVerifier = StepVerifier.create(dac.setHttpHeadersWithResponse(null, null))
        then:
        setPropertiesVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Set directory metadata"() {
        when:
        def directoryName = generatePathName()
        def dac = fsac.getDirectoryAsyncClient(directoryName)
        def setMetadataVerifier = StepVerifier.create(dac.setMetadataWithResponse(null, null))
        then:
        setMetadataVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Create file system"() {
        when:
        def fsac = getServiceAsyncClient(primaryCredential).getFileSystemAsyncClient(fileSystemName)
        def createVerifier = StepVerifier.create(fsac.createWithResponse(null, null))
        then:
        createVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Get file system properties"() {
        when:
        def fileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(primaryCredential).getFileSystemAsyncClient(fileSystemName)
        def getPropertiesVerifier = StepVerifier.create(fsac.getPropertiesWithResponse(null))
        then:
        getPropertiesVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Set file system metadata"() {
        when:
        def fileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(primaryCredential).getFileSystemAsyncClient(fileSystemName)
        def setMetadataVerifier = StepVerifier.create(fsac.setMetadataWithResponse(null, null))
        then:
        setMetadataVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Delete file system"() {
        when:
        def fileSystemName = generateFileSystemName()
        def fsac = getServiceAsyncClient(primaryCredential).getFileSystemAsyncClient(fileSystemName)
        def setMetadataVerifier = StepVerifier.create(fsac.deleteWithResponse(null))
        then:
        setMetadataVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

}
