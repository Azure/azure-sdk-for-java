// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.changefeed.pkversion.ServiceItemLease;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;

public class BootstrapperImplTests {

    private static final String BASE_CONTINUATION_STRING_FOR_EPK_FULL_RANGE = "{\"V\":1," +
        "\"Rid\":\"%s\"," +
        "\"Continuation\":[" +
        "{\"token\":\"%s\",\"range\":{\"min\":\"\",\"max\":\"FF\"}}" +
        "]," +
        "\"PKRangeId\":\"%s\"}";

    @DataProvider(name = "leaseProvider")
    public Object[][] leaseProvider() {

        String BASE_CONTINUATION_STRING_FOR_PK_FULL_RANGE = "\"100\"";

        return new Object[][] {
            {
                createEpkRangeBasedLeaseWithContinuation(
                    true,
                    ChangeFeedMode.FULL_FIDELITY,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                null,
                false
            },
            {
                createEpkRangeBasedLeaseWithContinuation(
                    true,
                    ChangeFeedMode.INCREMENTAL,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                null,
                true
            },
            {
                createEpkRangeBasedLeaseWithContinuation(
                    false,
                    ChangeFeedMode.INCREMENTAL,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                null,
                false
            },
            {
                createEpkRangeBasedLeaseWithContinuation(
                    false,
                    ChangeFeedMode.FULL_FIDELITY,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                null,
                false
            },
            {
                createEpkRangeBasedLeaseWithContinuation(
                    true,
                    ChangeFeedMode.FULL_FIDELITY,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                createPkRangeBasedLeaseWithContinuation(
                    true,
                    "XyJKUI7=",
                    "NO67Hq=",
                    "-FF",
                    BASE_CONTINUATION_STRING_FOR_PK_FULL_RANGE),
                true
            },
            {
                null,
                createPkRangeBasedLeaseWithContinuation(
                    true,
                    "XyJKUI7=",
                    "NO67Hq=",
                    "-FF",
                    BASE_CONTINUATION_STRING_FOR_PK_FULL_RANGE),
                true
            },
            {
                createEpkRangeBasedLeaseWithContinuation(
                    true,
                    ChangeFeedMode.FULL_FIDELITY,
                    ChangeFeedStartFromInternal.createFromNow(),
                    "XyJKUI7=",
                    "NO67Hq=",
                    "0",
                    "-FF",
                    "0"),
                null,
                false
            },
            {
                null,
                null,
                false
            }
        };
    }

    @Test(groups = {"unit"}, dataProvider = "leaseProvider")
    public void tryInitializeStoreFromEpkVersionLeaseStoreWithExistingLeases(
        ServiceItemLeaseV1 epkRangeBasedLease,
        ServiceItemLease pkRangeBasedLease,
        boolean expectIllegalStateException) {

        Duration lockTime = Duration.ofSeconds(5);
        Duration expireTIme = Duration.ofSeconds(5);

        PartitionSynchronizer partitionSynchronizerMock = Mockito.mock(PartitionSynchronizer.class);
        Mockito.when(partitionSynchronizerMock.createMissingLeases()).thenReturn(Mono.empty());

        LeaseStore leaseStoreMock = Mockito.mock(LeaseStore.class);
        Mockito
            .when(leaseStoreMock.isInitialized())
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.empty());

        LeaseStoreManager epkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        LeaseStoreManager pkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);

        ChangeFeedProcessorOptions changeFeedProcessorOptionsMock = Mockito.mock(ChangeFeedProcessorOptions.class);

        if (epkRangeBasedLease == null) {
            Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.empty());
        } else {
            Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.just(epkRangeBasedLease));
        }

        if (pkRangeBasedLease == null) {
            Mockito.when(pkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.empty());
        } else {
            Mockito.when(pkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1))).thenReturn(Flux.just(pkRangeBasedLease));
        }

        Bootstrapper bootstrapper = new BootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            epkRangeVersionLeaseStoreManagerMock,
            pkRangeVersionLeaseStoreManagerMock,
            changeFeedProcessorOptionsMock,
            ChangeFeedMode.FULL_FIDELITY);

        if (expectIllegalStateException) {
            Assert.assertThrows(IllegalStateException.class, () -> bootstrapper.initialize().block());
        } else {
            bootstrapper.initialize().block();
        }

        Mockito.verify(pkRangeVersionLeaseStoreManagerMock, times(1)).getTopLeases(Mockito.eq(1));

        if (pkRangeBasedLease == null) {
            Mockito.verify(epkRangeVersionLeaseStoreManagerMock, times(1)).getTopLeases(Mockito.eq(1));
        }

        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases();
        Mockito.verify(leaseStoreMock, times(2)).isInitialized();
    }

    @Test(groups = {"unit"}, dataProvider = "leaseProvider")
    public void tryInitializeStoreFromEpkVersionLeaseStoreWithExistingButMissingLeases(
        ServiceItemLeaseV1 epkRangeBasedLease,
        ServiceItemLease pkRangeBasedLease,
        boolean expectIllegalStateException) {

        Duration lockTime = Duration.ofSeconds(5);
        Duration expireTIme = Duration.ofSeconds(5);

        PartitionSynchronizer partitionSynchronizerMock = Mockito.mock(PartitionSynchronizer.class);
        Mockito.when(partitionSynchronizerMock.createMissingLeases()).thenReturn(Mono.empty());

        LeaseStore leaseStoreMock = Mockito.mock(LeaseStore.class);
        Mockito
            .when(leaseStoreMock.isInitialized())
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.empty());

        LeaseStoreManager epkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        LeaseStoreManager pkRangeVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);

        ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions()
            .setLeaseVerificationEnabledOnRestart(true);

        if (epkRangeBasedLease != null && expectIllegalStateException) {
            Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1)))
                   .thenReturn(Flux.just(epkRangeBasedLease));
        } else {
            Mockito.when(epkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1)))
                   .thenReturn(Flux.empty());
        }

        if (pkRangeBasedLease == null) {
            Mockito.when(pkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1)))
                   .thenReturn(Flux.empty());
        } else {
            Mockito.when(pkRangeVersionLeaseStoreManagerMock.getTopLeases(Mockito.eq(1)))
                   .thenReturn(Flux.just(pkRangeBasedLease));
        }

        Bootstrapper bootstrapper = new BootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            epkRangeVersionLeaseStoreManagerMock,
            pkRangeVersionLeaseStoreManagerMock,
            changeFeedProcessorOptions,
            ChangeFeedMode.FULL_FIDELITY);

        if (expectIllegalStateException) {
            Assert.assertThrows(IllegalStateException.class, () -> bootstrapper.initialize().block());
        } else {
            bootstrapper.initialize().block();
        }

        Mockito.verify(pkRangeVersionLeaseStoreManagerMock, times(1)).getTopLeases(Mockito.eq(1));

        if (pkRangeBasedLease == null) {
            Mockito.verify(epkRangeVersionLeaseStoreManagerMock, times(1)).getTopLeases(Mockito.eq(1));
            Mockito.verify(partitionSynchronizerMock, times(expectIllegalStateException ? 1 : 2)).createMissingLeases();
        } else {
            Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases();
        }

        Mockito.verify(leaseStoreMock, times(2)).isInitialized();
        Mockito.verify(leaseStoreMock, times(2)).acquireInitializationLock(Mockito.any());
    }

    private static ServiceItemLeaseV1 createEpkRangeBasedLeaseWithContinuation(
        boolean withContinuation,
        ChangeFeedMode changeFeedMode,
        ChangeFeedStartFromInternal startFromSettings,
        String databaseRid,
        String collectionRid,
        String pkRangeId,
        String leaseToken,
        String continuationToken) {

        ServiceItemLeaseV1 lease = new ServiceItemLeaseV1();

        lease.setId(String.format("%s_%s..%s", databaseRid, collectionRid, leaseToken));
        lease = lease.withLeaseToken(leaseToken);

        if (withContinuation) {
            FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRangeImpl = new FeedRangePartitionKeyRangeImpl(pkRangeId);

            String continuationAsJsonString = String.format(
                BASE_CONTINUATION_STRING_FOR_EPK_FULL_RANGE,
                collectionRid,
                continuationToken,
                pkRangeId);

            FeedRangeContinuation feedRangeContinuation = FeedRangeContinuation.convert(continuationAsJsonString);

            ChangeFeedState changeFeedState = new ChangeFeedStateV1(
                collectionRid,
                feedRangePartitionKeyRangeImpl,
                changeFeedMode,
                startFromSettings,
                feedRangeContinuation);

            lease.setContinuationToken(changeFeedState.toString());
        }

        return lease;
    }

    private static ServiceItemLease createPkRangeBasedLeaseWithContinuation(
        boolean withContinuation,
        String databaseRid,
        String collectionRid,
        String leaseToken,
        String continuationToken) {

        ServiceItemLease lease = new ServiceItemLease();

        lease.setId(String.format("%s_%s..%s", databaseRid, collectionRid, leaseToken));

        lease = lease.withLeaseToken(leaseToken);

        if (withContinuation) {
            lease = lease.withContinuationToken(continuationToken);
        }

        return lease;
    }
}
