package com.azure.storage.file.datalake.specialized;

import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeTestBase;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.PathProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LeaseAsyncApiTests  extends DataLakeTestBase {
    private DataLakeFileAsyncClient createPathClient() {
        DataLakeFileAsyncClient fc = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        fc.create().block();
        return fc;
    }

    @ParameterizedTest
    @MethodSource("acquireLeaseSupplier") //todo: after pr
    public void acquireFileLease(String proposedId, int leaseTime, LeaseStateType leaseStateType,
                                 LeaseDurationType leaseDurationType) {

    }

    private static Stream<Arguments> acquireLeaseSupplier() {
        return Stream.of(
            // proposedId | leaseTime | leaseStateType | leaseDurationType
            Arguments.of(null, -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE),
            Arguments.of(null, 25, LeaseStateType.LEASED, LeaseDurationType.FIXED),
            Arguments.of(CoreUtils.randomUuid().toString(), -1, LeaseStateType.LEASED, LeaseDurationType.INFINITE)
        );
    }





}
