// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmClientTestBase;
import com.azure.analytics.defender.easm.models.*;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiscoveryGroupsAsyncTest extends EasmClientTestBase {
    String knownGroupName = "University of Kansas";
    String newGroupName = "Async Test";
    String newGroupDescription = "Group created for async test";
    String seedKind = "domain";
    String seedName = "sampleSeed.org";

    private boolean doSeedsMatch(DiscoSource seedA, DiscoSource seedB) {
        return seedA.getKind() == seedB.getKind() && seedA.getName().equals(seedB.getName());
    }

    @Test
    public void testDiscoveryGroupsListAsync() {
        PagedFlux<DiscoGroup> discoGroupPagedFlux = easmAsyncClient.listDiscoGroup(null, 0);

        DiscoGroup discoGroup = discoGroupPagedFlux.blockFirst();
        assertNotNull(discoGroup.getName());
        assertNotNull(discoGroup.getDescription());
        assertNotNull(discoGroup.getTier());
        assertNotNull(discoGroup.getId());
    }

    @Test
    public void testDiscoveryGroupValidateAsync() {
        List<DiscoSource> seeds = Arrays.asList(new DiscoSource()
            .setKind(DiscoSourceKind.fromString(seedKind))
            .setName(seedName));
        DiscoGroupData discoGroupData = new DiscoGroupData()
            .setName("validate group name")
            .setDescription(newGroupDescription)
            .setSeeds(seeds)
            .setFrequencyMilliseconds(604800000L)
            .setTier("advanced");
        Mono<ValidateResult> validateResultMono = easmAsyncClient.validateDiscoGroup(discoGroupData);
        validateResultMono.subscribe(
            validateResult -> {
                assertNull(validateResult.getError());
            }
        );
    }

    @Test
    public void testDiscoveryGroupGetAsync() {
        Mono<DiscoGroup> discoGroupMono = easmAsyncClient.getDiscoGroup(knownGroupName);
        discoGroupMono.subscribe(
          discoGroup -> {
              assertEquals(knownGroupName, discoGroup.getId());
              assertEquals(knownGroupName, discoGroup.getName());
              assertEquals(knownGroupName, discoGroup.getDisplayName());
              assertNotNull(discoGroup.getDescription());
              assertNotNull(discoGroup.getTier());
          }
        );
    }

    @Test
    public void testDiscoveryGroupRunAsync() {
        assertDoesNotThrow(() -> easmAsyncClient.runDiscoGroup(knownGroupName));
    }

    @Test
    public void testDiscoveryGroupListRunsAsync() {
        Mono<DiscoRunPageResult> discoRunPageResultMono = easmAsyncClient.listRuns(knownGroupName, null, 0, 5);
        discoRunPageResultMono.subscribe(
            discoRunPageResult -> {
                List<DiscoRunResult> discoRunResults = discoRunPageResult.getValue();
                assertNotNull(discoRunResults);
                assertTrue(discoRunResults.size() > 2);
            }
        );
    }
}
