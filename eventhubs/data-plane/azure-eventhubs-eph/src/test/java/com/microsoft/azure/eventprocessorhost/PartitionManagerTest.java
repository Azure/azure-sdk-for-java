// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.EventHubClient;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class PartitionManagerTest extends TestBase {
    private ILeaseManager[] leaseManagers;
    private ICheckpointManager[] checkpointManagers;
    private EventProcessorHost[] hosts;
    private TestPartitionManager[] partitionManagers;
    private int partitionCount;
    private boolean[] running;

    private int countOfChecks;
    private int desiredDistributionDetected;

    private boolean keepGoing;
    private boolean expectEqualDistribution;
    private int overrideHostCount = -1;
    private int maxChecks;
    private boolean shuttingDown;

    @Test
    public void partitionBalancingExactMultipleTest() throws Exception {
        setup(2, 4, 0, 0); // two hosts, four partitions, no latency, default threadpool
        this.countOfChecks = 0;
        this.desiredDistributionDetected = 0;
        this.keepGoing = true;
        this.expectEqualDistribution = true;
        this.maxChecks = 20;
        startManagers();

        // Poll until checkPartitionDistribution() declares that it's time to stop.
        while (this.keepGoing) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                TestBase.logError("Sleep interrupted, emergency bail");
                Thread.currentThread().interrupt();
                throw e;
            }
        }

        stopManagers();

        assertTrue("Desired distribution never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

        this.leaseManagers[0].deleteLeaseStore().get();
        this.checkpointManagers[0].deleteCheckpointStore().get();
    }

    @Test
    public void partitionBalancingUnevenTest() throws Exception {
        setup(5, 16, 250, 0); // five hosts, sixteen partitions, 250ms latency, default threadpool
        this.countOfChecks = 0;
        this.desiredDistributionDetected = 0;
        this.keepGoing = true;
        this.expectEqualDistribution = false;
        this.maxChecks = 35;
        startManagers();

        // Poll until checkPartitionDistribution() declares that it's time to stop.
        while (this.keepGoing) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                TestBase.logError("Sleep interrupted, emergency bail");
                Thread.currentThread().interrupt();
                throw e;
            }
        }

        stopManagers();

        assertTrue("Desired distribution never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

        this.leaseManagers[0].deleteLeaseStore().get();
        this.checkpointManagers[0].deleteCheckpointStore().get();
    }


    @Test
    public void partitionBalancingHugeTest() throws Exception {
        setup(10, 201, 250, 20); // ten hosts, 201 partitions, 250ms latency, threadpool with 20 threads
        this.countOfChecks = 0;
        this.desiredDistributionDetected = 0;
        this.keepGoing = true;
        this.expectEqualDistribution = false;
        this.maxChecks = 99;
        startManagers();

        // Poll until checkPartitionDistribution() declares that it's time to stop.
        while (this.keepGoing) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                TestBase.logError("Sleep interrupted, emergency bail");
                Thread.currentThread().interrupt();
                throw e;
            }
        }

        stopManagers();

        assertTrue("Desired distribution never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

        this.leaseManagers[0].deleteLeaseStore().get();
        this.checkpointManagers[0].deleteCheckpointStore().get();
    }

    @Test
    public void partitionRebalancingTest() throws Exception {
        setup(3, 8, 0, 8); // three hosts, eight partitions, 250ms latency, default threadpool

        //
        // Start two hosts of three, expect 4/4/0.
        //
        this.countOfChecks = 0;
        this.desiredDistributionDetected = 0;
        this.keepGoing = true;
        this.expectEqualDistribution = true; // only going to start two of the three hosts
        this.maxChecks = 20;
        this.overrideHostCount = 2;
        startManagers(2);
        while (this.keepGoing) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                TestBase.logError("Sleep interrupted, emergency bail");
                Thread.currentThread().interrupt();
                throw e;
            }
        }
        assertTrue("Desired distribution 4/4/0 never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

        //
        // Start up the third host and wait for rebalance
        //
        this.countOfChecks = 0;
        this.desiredDistributionDetected = 0;
        this.keepGoing = true;
        this.expectEqualDistribution = false;
        this.maxChecks = 30;
        this.overrideHostCount = 3;
        startSingleManager(2);
        while (this.keepGoing) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                TestBase.logError("Sleep interrupted, emergency bail");
                Thread.currentThread().interrupt();
                throw e;
            }
        }
        assertTrue("Desired distribution never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

        //
        // Now stop host 0 and wait for 0/4/4
        //
        this.countOfChecks = 0;
        this.desiredDistributionDetected = 0;
        this.keepGoing = true;
        this.expectEqualDistribution = true; // only two of the three hosts running
        this.maxChecks = 20;
        this.overrideHostCount = 2;
        stopSingleManager(0);
        while (this.keepGoing) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                TestBase.logError("Sleep interrupted, emergency bail");
                Thread.currentThread().interrupt();
                throw e;
            }
        }
        assertTrue("Desired distribution 4/4/0 never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

        stopManagers();

        this.leaseManagers[1].deleteLeaseStore().get();
        this.checkpointManagers[1].deleteCheckpointStore().get();
    }

    @Test
    public void partitionBalancingTooManyHostsTest() throws Exception {
        setup(10, 4, 0, 8); // ten hosts, four partitions
        this.countOfChecks = 0;
        this.desiredDistributionDetected = 0;
        this.keepGoing = true;
        this.expectEqualDistribution = false;
        this.maxChecks = 20;
        startManagers();

        // Poll until checkPartitionDistribution() declares that it's time to stop.
        while (this.keepGoing) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                TestBase.logError("Sleep interrupted, emergency bail");
                Thread.currentThread().interrupt();
                throw e;
            }
        }

        stopManagers();

        assertTrue("Desired distribution never reached or was not stable", this.desiredDistributionDetected >= this.partitionManagers.length);

        this.leaseManagers[0].deleteLeaseStore().get();
        this.checkpointManagers[0].deleteCheckpointStore().get();
    }

    synchronized void checkPartitionDistribution() {
        if (this.shuttingDown) {
            return;
        }

        TestBase.logInfo("Checking partition distribution");
        int[] countsPerHost = new int[this.partitionManagers.length];
        int totalCounts = 0;
        int runningCount = 0;
        for (int i = 0; i < this.partitionManagers.length; i++) {
            StringBuilder blah = new StringBuilder();
            blah.append("\tHost ");
            blah.append(this.hosts[i].getHostContext().getHostName());
            blah.append(" has ");
            countsPerHost[i] = 0;
            for (String id : this.partitionManagers[i].getOwnedPartitions()) {
                blah.append(id);
                blah.append(", ");
                countsPerHost[i]++;
                totalCounts++;
            }
            TestBase.logInfo(blah.toString());
            if (this.running[i]) {
                runningCount++;
            }
        }

        if (totalCounts != this.partitionCount) {
            TestBase.logInfo("Unowned leases, " + totalCounts + " owned versus " + this.partitionCount + " partitions, skipping checks");
            return;
        }
        if (this.overrideHostCount > 0) {
            if (runningCount != this.overrideHostCount) {
                TestBase.logInfo("Hosts not running, " + this.overrideHostCount + " expected versus " + runningCount + " found, skipping checks");
                return;
            }
        } else if (runningCount != this.partitionManagers.length) {
            TestBase.logInfo("Hosts not running, " + this.partitionManagers.length + " expected versus " + runningCount + " found, skipping checks");
            return;
        }

        boolean desired = true;
        int highest = Integer.MIN_VALUE;
        int lowest = Integer.MAX_VALUE;
        for (int i = 0; i < countsPerHost.length; i++) {
            if (this.running[i]) {
                highest = Integer.max(highest, countsPerHost[i]);
                lowest = Integer.min(lowest, countsPerHost[i]);
            }
        }
        TestBase.logInfo("Check " + this.countOfChecks + "  Highest " + highest + "  Lowest " + lowest + "  Descnt " + this.desiredDistributionDetected);
        if (this.expectEqualDistribution) {
            // All hosts should have exactly equal counts, so highest == lowest
            desired = (highest == lowest);
        } else {
            // An equal distribution isn't possible, but the maximum difference between counts should be 1.
            // Max(counts[]) - Min(counts[]) == 1
            desired = ((highest - lowest) == 1);
        }
        if (desired) {
            TestBase.logInfo("Evenest distribution detected");
            this.desiredDistributionDetected++;
            if (this.desiredDistributionDetected > this.partitionManagers.length) {
                // Every partition manager has looked at the current distribution and
                // it has not changed. The algorithm is stable once it reaches the desired state.
                // No need to keep iterating.
                TestBase.logInfo("Desired distribution is stable");
                this.keepGoing = false;
            }
        } else {
            if ((this.desiredDistributionDetected > 0) && !this.shuttingDown) {
                // If we have detected the desired distribution on previous iterations
                // but not on this one, then the algorithm is unstable. Bail and fail.
                TestBase.logInfo("Desired distribution was not stable");
                this.keepGoing = false;
            }
        }

        this.countOfChecks++;
        if (this.countOfChecks > this.maxChecks) {
            // Ran out of iterations without reaching the desired distribution. Bail and fail.
            this.keepGoing = false;
        }
    }

    private void setup(int hostCount, int partitionCount, long latency, int threads) {
        // PartitionManager tests are all long. Skip if running automated (maven, appveyor, etc.)
        skipIfAutomated();

        this.leaseManagers = new ILeaseManager[hostCount];
        this.checkpointManagers = new ICheckpointManager[hostCount];
        this.hosts = new EventProcessorHost[hostCount];
        this.partitionManagers = new TestPartitionManager[hostCount];
        this.partitionCount = partitionCount;
        this.running = new boolean[hostCount];

        for (int i = 0; i < hostCount; i++) {
            InMemoryLeaseManager lm = new InMemoryLeaseManager();
            InMemoryCheckpointManager cm = new InMemoryCheckpointManager();

            // In order to test hosts competing for partitions, each host must have a unique name, but they must share the
            // target eventhub/consumer group.
            ScheduledExecutorService threadpool = null;
            if (threads > 0) {
                threadpool = Executors.newScheduledThreadPool(threads);
            }
            this.hosts[i] = new EventProcessorHost("dummyHost" + String.valueOf(i), "NOTREAL", EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
                    RealEventHubUtilities.SYNTACTICALLY_CORRECT_DUMMY_CONNECTION_STRING, cm, lm, threadpool, null);

            lm.initialize(this.hosts[i].getHostContext());
            lm.setLatency(latency);
            this.leaseManagers[i] = lm;
            cm.initialize(this.hosts[i].getHostContext());
            this.checkpointManagers[i] = cm;
            this.running[i] = false;

            this.partitionManagers[i] = new TestPartitionManager(this.hosts[i].getHostContext(), partitionCount);
            this.hosts[i].setPartitionManager(this.partitionManagers[i]);
            this.hosts[i].getHostContext().setEventProcessorOptions(EventProcessorOptions.getDefaultOptions());
            // Quick lease expiration helps with some tests. Because we're using InMemoryLeaseManager, don't
            // have to worry about storage latency, all lease operations are guaranteed to be fast.
            PartitionManagerOptions opts = new PartitionManagerOptions();
            opts.setLeaseDurationInSeconds(15);
            //opts.setStartupScanDelayInSeconds(17);
            //opts.setSlowScanIntervalInSeconds(15);
            this.hosts[i].setPartitionManagerOptions(opts);
        }
    }

    private void startManagers() throws Exception {
        startManagers(this.partitionManagers.length);
    }

    private void startManagers(int maxIndex) throws Exception {
        this.shuttingDown = false;
        for (int i = 0; i < maxIndex; i++) {
            startSingleManager(i);
        }
    }

    private void startSingleManager(int index) throws Exception {
        try {
            this.partitionManagers[index].initialize().get();
            this.running[index] = true;
        } catch (Exception e) {
            TestBase.logError("TASK START FAILED " + e.toString() + " " + e.getMessage());
            throw e;
        }
    }

    private void stopManagers() throws InterruptedException, ExecutionException {
        TestBase.logInfo("SHUTTING DOWN");
        this.shuttingDown = true;
        for (int i = 0; i < this.partitionManagers.length; i++) {
            if (this.running[i]) {
                this.partitionManagers[i].stopPartitions().get();
                TestBase.logInfo("Host " + i + " stopped");
            }
        }
    }

    private void stopSingleManager(int index) throws InterruptedException, ExecutionException {
        if (this.running[index]) {
            this.partitionManagers[index].stopPartitions().get();
            TestBase.logInfo("Host " + index + " stopped");
            this.running[index] = false;
        }
    }

    private class TestPartitionManager extends PartitionManager {
        private int partitionCount;

        TestPartitionManager(HostContext hostContext, int partitionCount) {
            super(hostContext);
            this.partitionCount = partitionCount;
        }

        Iterable<String> getOwnedPartitions() {
            Iterable<String> retval = null;
            if (this.pumpManager != null) {
                retval = ((DummyPump) this.pumpManager).getPumpsList();
            } else {
                // If the manager isn't started, return an empty list.
                retval = new ArrayList<String>();
            }
            return retval;
        }

        @Override
        CompletableFuture<Void> cachePartitionIds() {
            this.partitionIds = new String[this.partitionCount];
            for (int i = 0; i < this.partitionCount; i++) {
                this.partitionIds[i] = String.valueOf(i);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        PumpManager createPumpTestHook() {
            return new DummyPump(this.hostContext, this);
        }

        @Override
        void onInitializeCompleteTestHook() {
            TestBase.logInfo("PartitionManager for host " + this.hostContext.getHostName() + " initialized stores OK");
        }

        @Override
        void onPartitionCheckCompleteTestHook() {
            PartitionManagerTest.this.checkPartitionDistribution();
        }
    }
}
