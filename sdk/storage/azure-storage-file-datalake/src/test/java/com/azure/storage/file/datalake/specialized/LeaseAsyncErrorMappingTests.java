// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake.specialized;

import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeTestBase;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class LeaseAsyncErrorMappingTests extends DataLakeTestBase {
    private DataLakeFileAsyncClient createPathAsyncClient() {
        return getServiceAsyncClient(ENVIRONMENT.getDataLakeAccount())
            .createFileSystem(generateFileSystemName())
            .map(fsac -> fsac.getFileAsyncClient(generatePathName()))
            .block();
    }

    private DataLakeLeaseAsyncClient leaseAsyncClient;

    @BeforeEach
    public void setup() {
        DataLakeFileAsyncClient fac = createPathAsyncClient();
        leaseAsyncClient = createLeaseAsyncClient(fac);
    }

    @Test
    public void acquireLease() {
        StepVerifier.create(leaseAsyncClient.acquireLeaseWithResponse(-10, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void renewLease() {
        StepVerifier.create(leaseAsyncClient.renewLeaseWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void releaseLease() {
        StepVerifier.create(leaseAsyncClient.releaseLeaseWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void changeLease() {
        StepVerifier.create(leaseAsyncClient.changeLeaseWithResponse("id", null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void breakLease() {
        StepVerifier.create(leaseAsyncClient.breakLeaseWithResponse(null, null))
            .verifyError(DataLakeStorageException.class);
    }
}
