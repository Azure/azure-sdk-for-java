// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.epkversion.ServiceItemLeaseV1;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EqualPartitionsBalancingStrategyTests {

    @Test(groups = "unit")
    public void expiredLeases_legacyClampsToOneWhenMultipleWorkers() {
        String hostName = "me";
        List<Lease> allLeases = new ArrayList<>();

        // Multiple workers exist because there are existing owners in the lease set.
        for (int i = 0; i < 10; i++) {
            allLeases.add(newLease("unowned-" + i, null));
            allLeases.add(newLease("old1-" + i, "old1", Instant.now()));
            allLeases.add(newLease("old2-" + i, "old2", Instant.now()));
        }

        EqualPartitionsBalancingStrategy strategy =
            new EqualPartitionsBalancingStrategy(hostName, 0, 0, Duration.ofSeconds(60));

        List<Lease> leasesToTake = strategy.selectLeasesToTake(allLeases);
        assertEquals(leasesToTake.size(), 1);
    }

    @Test(groups = "unit")
    public void expiredLeases_allowsMultipleWhenConfigured() {
        String hostName = "me";
        List<Lease> allLeases = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            allLeases.add(newLease("unowned-" + i, null));
            allLeases.add(newLease("old1-" + i, "old1", Instant.now()));
            allLeases.add(newLease("old2-" + i, "old2", Instant.now()));
        }

        EqualPartitionsBalancingStrategy strategy =
            new EqualPartitionsBalancingStrategy(hostName, 0, 0, Duration.ofSeconds(60), 5);

        List<Lease> leasesToTake = strategy.selectLeasesToTake(allLeases);
        assertEquals(leasesToTake.size(), 5);
        assertUniqueLeaseTokens(leasesToTake);
    }

    @Test(groups = "unit")
    public void stealLeases_legacyClampsToOne() {
        String hostName = "me";
        List<Lease> allLeases = new ArrayList<>();

        // No expired leases: everything is owned by a single other worker.
        for (int i = 0; i < 30; i++) {
            allLeases.add(newLease("old-" + i, "old", Instant.now()));
        }

        EqualPartitionsBalancingStrategy strategy =
            new EqualPartitionsBalancingStrategy(hostName, 0, 0, Duration.ofSeconds(60));

        List<Lease> leasesToTake = strategy.selectLeasesToTake(allLeases);
        assertEquals(leasesToTake.size(), 1);
        assertEquals(leasesToTake.get(0).getOwner(), "old");
    }

    @Test(groups = "unit")
    public void stealLeases_stillClampsToOneWhenConfigured() {
        String hostName = "me";
        List<Lease> allLeases = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            allLeases.add(newLease("old-" + i, "old", Instant.now()));
        }

        EqualPartitionsBalancingStrategy strategy =
            new EqualPartitionsBalancingStrategy(hostName, 0, 0, Duration.ofSeconds(60), 5);

        List<Lease> leasesToTake = strategy.selectLeasesToTake(allLeases);
        // Multi-acquire is only for unused/expired leases; stealing intentionally keeps the legacy 1-lease-per-cycle behavior.
        assertEquals(leasesToTake.size(), 1);
        assertUniqueLeaseTokens(leasesToTake);

        for (Lease lease : leasesToTake) {
            assertEquals(lease.getOwner(), "old");
        }
    }

    private static ServiceItemLeaseV1 newLease(String token, String owner) {
        return newLease(token, owner, null);
    }

    private static ServiceItemLeaseV1 newLease(String token, String owner, Instant timestamp) {
        ServiceItemLeaseV1 lease = new ServiceItemLeaseV1()
            .withLeaseToken(token)
            .withOwner(owner);
        if (timestamp != null) {
            lease.withTimestamp(timestamp);
        }
        lease.setId("lease-" + token);
        return lease;
    }

    private static void assertUniqueLeaseTokens(List<Lease> leases) {
        Set<String> tokens = new HashSet<>();
        for (Lease lease : leases) {
            assertTrue(tokens.add(lease.getLeaseToken()), "Duplicate lease token: " + lease.getLeaseToken());
        }
    }
}
