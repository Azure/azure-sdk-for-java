package com.azure.storage.file.datalake.specialized

import com.azure.storage.file.datalake.APISpec
import com.azure.storage.file.datalake.DataLakeFileAsyncClient
import com.azure.storage.file.datalake.models.DataLakeStorageException
import reactor.test.StepVerifier

class LeaseAsyncErrorMappingTest extends APISpec {
    private DataLakeFileAsyncClient createPathAsyncClient() {
        def fac = getServiceAsyncClient(primaryCredential)
            .createFileSystem(generateFileSystemName()).block()
            .getFileAsyncClient(generatePathName())
        return fac
    }

    DataLakeFileAsyncClient fac
    DataLakeLeaseAsyncClient leaseAsyncClient

    def setup() {
        fac = createPathAsyncClient()
        leaseAsyncClient = createLeaseAsyncClient(fac)
    }

    def "Acquire Lease"() {
        when:
        def acquireLeaseVerifier = StepVerifier.create(leaseAsyncClient.acquireLeaseWithResponse(-10, null))
        then:
        acquireLeaseVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Renew Lease"() {
        when:
        def renewLeaseVerifier = StepVerifier.create(leaseAsyncClient.renewLeaseWithResponse(null))
        then:
        renewLeaseVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Release Lease"() {
        when:
        def releaseLeaseVerifier = StepVerifier.create(leaseAsyncClient.releaseLeaseWithResponse(null))
        then:
        releaseLeaseVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Change Lease"() {
        when:
        def changeLeaseVerifier = StepVerifier.create(leaseAsyncClient.changeLeaseWithResponse(null, null))
        then:
        changeLeaseVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

    def "Break Lease"() {
        when:
        def breakLeaseVerifier = StepVerifier.create(leaseAsyncClient.breakLeaseWithResponse(null, null))
        then:
        breakLeaseVerifier.verifyErrorSatisfies {
            assert it instanceof DataLakeStorageException
        }
    }

}
