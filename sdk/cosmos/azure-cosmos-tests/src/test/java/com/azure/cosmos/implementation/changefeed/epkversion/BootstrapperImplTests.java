// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.PartitionController;
import com.azure.cosmos.implementation.changefeed.PartitionLoadBalancer;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.changefeed.pkversion.ServiceItemLease;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;

public class BootstrapperImplTests {

    private static final String BASE_CONTINUATION_STRING_FOR_EPK_FULL_RANGE = "{\"V\":1," +
        "\"Rid\":\"%s\"," +
        "\"Continuation\":[" +
        "{\"token\":\"%s\",\"range\":{\"min\":\"\",\"max\":\"FF\"}}" +
        "]," +
        "\"PKRangeId\":\"%s\"}";

    private enum InitializationFailure {
        DISCOVERY_ERROR,
        EMPTY_RANGES,
        LEASE_CREATION_ERROR
    }

    @DataProvider
    public Object[][] initializationFailures() {
        return new Object[][] {
            {ChangeFeedMode.FULL_FIDELITY, InitializationFailure.DISCOVERY_ERROR},
            {ChangeFeedMode.FULL_FIDELITY, InitializationFailure.EMPTY_RANGES},
            {ChangeFeedMode.FULL_FIDELITY, InitializationFailure.LEASE_CREATION_ERROR},
            {ChangeFeedMode.INCREMENTAL, InitializationFailure.DISCOVERY_ERROR},
            {ChangeFeedMode.INCREMENTAL, InitializationFailure.EMPTY_RANGES},
            {ChangeFeedMode.INCREMENTAL, InitializationFailure.LEASE_CREATION_ERROR}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "initializationFailures")
    public void initializationFailureLeavesStoreUnmarkedAndRetryCreatesMissingLeases(
        ChangeFeedMode mode, InitializationFailure failureType) {

        Duration lockTime = Duration.ofSeconds(5);
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();
        ChangeFeedContextClient contextClient = Mockito.mock(ChangeFeedContextClient.class);
        LeaseStoreManager epkStore = Mockito.mock(LeaseStoreManager.class);
        LeaseStoreManager pkStore = Mockito.mock(LeaseStoreManager.class);
        PartitionKeyRange firstRange = new PartitionKeyRange("0", "", "AA");
        PartitionKeyRange secondRange = new PartitionKeyRange("1", "AA", "FF");
        List<PartitionKeyRange> ranges = Arrays.asList(firstRange, secondRange);
        FeedRangeEpkImpl firstFeedRange = new FeedRangeEpkImpl(firstRange.toRange());
        FeedRangeEpkImpl secondFeedRange = new FeedRangeEpkImpl(secondRange.toRange());
        Lease firstLease = new ServiceItemLeaseV1().withLeaseToken("-AA").withFeedRange(firstFeedRange);
        Lease secondLease = new ServiceItemLeaseV1().withLeaseToken("AA-FF").withFeedRange(secondFeedRange);
        RuntimeException failure = new IllegalStateException("Initialization failed");

        boolean leaseCreationFails = failureType == InitializationFailure.LEASE_CREATION_ERROR;
        Mono<List<PartitionKeyRange>> initialDiscovery;
        switch (failureType) {
            case DISCOVERY_ERROR:
                initialDiscovery = Mono.error(failure);
                break;
            case EMPTY_RANGES:
                initialDiscovery = Mono.just(Collections.emptyList());
                break;
            default:
                initialDiscovery = Mono.just(ranges);
                break;
        }
        Mockito.when(contextClient.getOverlappingRanges(PartitionKeyInternalHelper.FullRange, true))
            .thenReturn(initialDiscovery, Mono.just(ranges));
        Mockito.when(epkStore.getAllLeases())
            .thenReturn(Flux.empty(), Flux.just(firstLease));
        Mockito.when(epkStore.createLeaseIfNotExist(Mockito.any(FeedRangeEpkImpl.class), Mockito.isNull()))
            .thenAnswer(invocation -> {
                FeedRangeEpkImpl feedRange = invocation.getArgument(0);
                return feedRange.getRange().equals(firstRange.toRange())
                    ? Mono.just(firstLease)
                    : Mono.just(secondLease);
            });
        if (leaseCreationFails) {
            Mockito.when(epkStore.createLeaseIfNotExist(Mockito.eq(secondFeedRange), Mockito.isNull()))
                .thenReturn(Mono.error(failure), Mono.just(secondLease));
        }

        Mockito.when(epkStore.isInitialized()).thenReturn(Mono.just(false), Mono.just(false), Mono.just(true));
        Mockito.when(epkStore.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(epkStore.releaseInitializationLock()).thenReturn(Mono.just(true));
        Mockito.when(epkStore.markInitialized()).thenReturn(Mono.just(true));
        Mockito.when(epkStore.getTopLeases(1)).thenReturn(Flux.empty());
        Mockito.when(pkStore.isInitialized()).thenReturn(Mono.just(false));
        Mockito.when(pkStore.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(pkStore.releaseInitializationLock()).thenReturn(Mono.just(true));
        Mockito.when(pkStore.markInitialized()).thenReturn(Mono.just(true));
        Mockito.when(pkStore.getTopLeases(1)).thenReturn(Flux.empty());

        RuntimeException epkReleaseFailure = new IllegalStateException("EPK lock release failed");
        RuntimeException pkReleaseFailure = new IllegalStateException("PK lock release failed");
        if (leaseCreationFails) {
            Mockito.when(epkStore.releaseInitializationLock()).thenReturn(Mono.error(epkReleaseFailure), Mono.just(true));
            Mockito.when(pkStore.releaseInitializationLock()).thenReturn(Mono.error(pkReleaseFailure), Mono.just(true));
        }

        PartitionSynchronizer synchronizer = new PartitionSynchronizerImpl(
            contextClient, "container", epkStore, epkStore, 1, 1, options, mode);
        Bootstrapper bootstrapper = mode == ChangeFeedMode.INCREMENTAL
            ? new PkRangeIdVersionLeaseStoreBootstrapperImpl(
                synchronizer, epkStore, lockTime, lockTime, pkStore, epkStore, options, mode)
            : new BootstrapperImpl(
                synchronizer, epkStore, lockTime, lockTime, epkStore, pkStore, options, mode);

        PartitionController controller = Mockito.mock(PartitionController.class);
        PartitionLoadBalancer loadBalancer = Mockito.mock(PartitionLoadBalancer.class);
        Mockito.when(controller.initialize()).thenReturn(Mono.empty());
        Mockito.when(loadBalancer.start()).thenReturn(Mono.empty());
        PartitionManagerImpl manager = new PartitionManagerImpl(bootstrapper, controller, loadBalancer);

        StepVerifier.create(manager.start())
            .expectErrorSatisfies(error -> {
                if (failureType == InitializationFailure.EMPTY_RANGES) {
                    Assert.assertTrue(error instanceof IllegalStateException);
                    Assert.assertEquals(error.getMessage(), "No partition key ranges found for change feed initialization");
                } else {
                    Assert.assertSame(error, failure);
                }
                Throwable[] expectedSuppressed = !leaseCreationFails
                    ? new Throwable[0]
                    : mode == ChangeFeedMode.INCREMENTAL
                        ? new Throwable[] {epkReleaseFailure, pkReleaseFailure}
                        : new Throwable[] {epkReleaseFailure};
                Assert.assertEquals(error.getSuppressed(), expectedSuppressed);
            })
            .verify(lockTime);
        if (!leaseCreationFails) {
            Mockito.verify(epkStore, Mockito.never())
                .createLeaseIfNotExist(Mockito.any(FeedRangeEpkImpl.class), Mockito.isNull());
        }
        Mockito.verify(epkStore, Mockito.never()).markInitialized();
        Mockito.verify(pkStore, Mockito.never()).markInitialized();
        Mockito.verify(epkStore).releaseInitializationLock();
        if (mode == ChangeFeedMode.INCREMENTAL) {
            Mockito.verify(pkStore).releaseInitializationLock();
        }

        Mockito.verifyNoInteractions(controller, loadBalancer);
        StepVerifier.create(manager.start()).expectComplete().verify(lockTime);
        Mockito.verify(controller).initialize();
        Mockito.verify(loadBalancer).start();
        Mockito.verify(epkStore).createLeaseIfNotExist(Mockito.eq(firstFeedRange), Mockito.isNull());
        Mockito.verify(epkStore, times(leaseCreationFails ? 2 : 1))
            .createLeaseIfNotExist(Mockito.eq(secondFeedRange), Mockito.isNull());
        Mockito.verify(epkStore).markInitialized();
        if (mode == ChangeFeedMode.INCREMENTAL) {
            Mockito.verify(pkStore).markInitialized();
        }
    }

    @DataProvider
    public Object[][] epkChangeFeedModes() {
        return new Object[][] {{ChangeFeedMode.FULL_FIDELITY}, {ChangeFeedMode.INCREMENTAL}};
    }

    @Test(groups = {"unit"}, dataProvider = "epkChangeFeedModes")
    public void verifiesLeasesOnRestartAfterLockAcquisitionIsRetried(ChangeFeedMode mode) {
        Duration lockTime = Duration.ofSeconds(5);
        Duration sleepTime = Duration.ofMillis(1);
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();
        PartitionSynchronizer synchronizer = Mockito.mock(PartitionSynchronizer.class);
        LeaseStoreManager epkStore = Mockito.mock(LeaseStoreManager.class);
        LeaseStoreManager pkStore = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(epkStore.isInitialized()).thenReturn(Mono.just(true));
        Mockito.when(epkStore.getTopLeases(1)).thenReturn(Flux.empty());
        Mockito.when(pkStore.getTopLeases(1)).thenReturn(Flux.empty());
        Mockito.when(epkStore.acquireInitializationLock(lockTime)).thenReturn(Mono.just(false), Mono.just(true));
        Mockito.when(epkStore.releaseInitializationLock()).thenReturn(Mono.just(true));
        Mockito.when(synchronizer.createMissingLeases()).thenReturn(Mono.empty());
        Bootstrapper bootstrapper = mode == ChangeFeedMode.INCREMENTAL
            ? new PkRangeIdVersionLeaseStoreBootstrapperImpl(
                synchronizer, epkStore, lockTime, sleepTime, pkStore, epkStore, options, mode)
            : new BootstrapperImpl(
                synchronizer, epkStore, lockTime, sleepTime, epkStore, pkStore, options, mode);

        StepVerifier.create(bootstrapper.initialize()).expectComplete().verify(lockTime);
        Mockito.verifyNoInteractions(synchronizer);
        Mockito.verify(epkStore, Mockito.never()).acquireInitializationLock(lockTime);

        options.setLeaseVerificationEnabledOnRestart(true);
        StepVerifier.create(bootstrapper.initialize()).expectComplete().verify(lockTime);
        Mockito.verify(synchronizer).createMissingLeases();
        Mockito.verify(epkStore, times(2)).acquireInitializationLock(lockTime);
        Mockito.verify(epkStore).releaseInitializationLock();
        Mockito.verify(epkStore, Mockito.never()).markInitialized();
        Mockito.verify(pkStore, Mockito.never()).acquireInitializationLock(lockTime);
    }

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
