// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.*;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiscoveryGroupsAsyncTest extends EasmClientTestBase {
    String knownGroupName = "University of Kansas";
    String newGroupName = "Async Test";
    String newGroupDescription = "Group created for async test";
    String seedKind = "domain";
    String seedName = "sampleseed.org";

    private boolean doSeedsMatch(DiscoSource seedA, DiscoSource seedB) {
        return seedA.getKind() == seedB.getKind() && seedA.getName().equals(seedB.getName());
    }

    @Test
    public void testDiscoveryGroupsListAsync() {
        List<DiscoGroup> discoGroupList = new ArrayList<>();
        PagedFlux<DiscoGroup> discoGroupPagedFlux = easmAsyncClient.listDiscoGroup(null, 0);
        StepVerifier.create(discoGroupPagedFlux)
            .thenConsumeWhile(discoGroupList::add)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        for (DiscoGroup discoGroup : discoGroupList) {
            assertNotNull(discoGroup.getName());
            assertNotNull(discoGroup.getDescription());
            assertNotNull(discoGroup.getTier());
            assertNotNull(discoGroup.getId());
        }
    }

    @Test
    public void testDiscoveryGroupValidateAsync() {
        List<DiscoSource> seeds
            = Arrays.asList(new DiscoSource().setKind(DiscoSourceKind.fromString(seedKind)).setName(seedName));
        DiscoGroupData discoGroupData = new DiscoGroupData().setName(newGroupName)
            .setDescription(newGroupDescription)
            .setSeeds(seeds)
            .setFrequencyMilliseconds(604800000L)
            .setTier("advanced");

        Mono<ValidateResult> validateResultMono = easmAsyncClient.validateDiscoGroup(discoGroupData);
        StepVerifier.create(validateResultMono).assertNext(validateResult -> {
            assertNull(validateResult.getError());
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void testDiscoveryGroupGetAsync() {
        Mono<DiscoGroup> discoGroupMono = easmAsyncClient.getDiscoGroup(knownGroupName);
        StepVerifier.create(discoGroupMono).assertNext(discoGroup -> {
            assertEquals(knownGroupName, discoGroup.getId());
            assertEquals(knownGroupName, discoGroup.getName());
            assertEquals(knownGroupName, discoGroup.getDisplayName());
            assertNotNull(discoGroup.getDescription());
            assertNotNull(discoGroup.getTier());
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void testDiscoveryGroupRunAsync() {
        Mono<Void> runDiscoGroupMono = easmAsyncClient.runDiscoGroup(knownGroupName);
        StepVerifier.create(runDiscoGroupMono).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void testDiscoveryGroupListRunsAsync() {
        PagedFlux<DiscoRunResult> discoRunResultPagedFlux = easmAsyncClient.listRuns(knownGroupName, null, 0);
        List<DiscoRunResult> discoRunResults = new ArrayList<>();

        StepVerifier.create(discoRunResultPagedFlux)
            .thenConsumeWhile(discoRunResults::add)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
        assertTrue(discoRunResults.size() > 2);
        for (DiscoRunResult discoRunResult : discoRunResults) {
            assertNotNull(discoRunResult.getState());
        }
    }
}
