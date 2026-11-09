// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LeaseStoreManagerDeleteAllTest {
    @DataProvider
    public Object[][] leaseStoreManagers() {
        return new Object[][] {
            { new com.azure.cosmos.implementation.changefeed.pkversion.LeaseStoreManagerImpl() },
            { new com.azure.cosmos.implementation.changefeed.epkversion.LeaseStoreManagerImpl() }
        };
    }

    @Test(groups = "unit", dataProvider = "leaseStoreManagers")
    public void deleteAllCompletesWhenLeaseIsAlreadyAbsent(LeaseStoreManager leaseStoreManager) {
        String leaseId = "lease";
        Lease lease = mock(Lease.class);
        when(lease.getId()).thenReturn(leaseId);

        ChangeFeedContextClient leaseContextClient = mock(ChangeFeedContextClient.class);
        ((LeaseStoreManager.LeaseStoreManagerBuilderDefinition) leaseStoreManager)
            .leaseContextClient(leaseContextClient);

        CosmosItemOperation operation = CosmosBulkOperations.getDeleteItemOperation(leaseId, new PartitionKey(leaseId));
        CosmosBulkItemResponse itemResponse = new CosmosBulkItemResponse(
            null, 0, null, 404, null, 0, Collections.emptyMap(), null, null);
        CosmosBulkOperationResponse<Object> response = new CosmosBulkOperationResponse<>(operation, itemResponse, null);
        when(leaseContextClient.deleteAllItems(anyList()))
            .thenReturn(Flux.just(response))
            .thenReturn(Flux.error(new AssertionError("An absent lease must not be retried")));

        StepVerifier.create(leaseStoreManager.deleteAll(Collections.singletonList(lease)))
            .expectComplete()
            .verify(Duration.ofSeconds(5));

        verify(leaseContextClient).deleteAllItems(anyList());
    }
}
