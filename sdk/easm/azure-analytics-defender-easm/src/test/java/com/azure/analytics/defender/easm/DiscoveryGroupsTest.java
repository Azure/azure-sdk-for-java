// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.*;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiscoveryGroupsTest extends EasmClientTestBase {

    String knownGroupName = "University of Kansas";
    String newGroupName = "New disco group Name";
    String newGroupDescription = "This is a description";
    String seedKind = "domain";
    String seedName = "example.org";

    private boolean doSeedsMatch(DiscoSource seedA, DiscoSource seedB) {
        return seedA.getKind() == seedB.getKind() && seedA.getName().equals(seedB.getName());
    }

    @Test
    public void testdiscoveryGroupsListWithResponse() {
        PagedIterable<DiscoGroup> discoGroups = easmClient.listDiscoGroup(null, 0);
        DiscoGroup discoGroup = discoGroups.stream().iterator().next();
        assertNotNull(discoGroup.getName());
        assertNotNull(discoGroup.getDescription());
        assertNotNull(discoGroup.getTier());
        assertNotNull(discoGroup.getId());
    }

    @Test
    public void testdiscoveryGroupsValidateWithResponse() {
        List<DiscoSource> seeds
            = Arrays.asList(new DiscoSource().setKind(DiscoSourceKind.fromString(seedKind)).setName(seedName));
        DiscoGroupData discoGroupData = new DiscoGroupData().setName("validate group name")
            .setDescription(newGroupDescription)
            .setSeeds(seeds)
            .setFrequencyMilliseconds(604800000L)
            .setTier("advanced");
        ValidateResult validateResponse = easmClient.validateDiscoGroup(discoGroupData);
        assertNull(validateResponse.getError());
    }

    @Test
    public void testdiscoveryGroupsGetWithResponse() {
        DiscoGroup discoGroupResponse = easmClient.getDiscoGroup(knownGroupName);
        assertEquals(knownGroupName, discoGroupResponse.getId());
        assertEquals(knownGroupName, discoGroupResponse.getName());
        assertEquals(knownGroupName, discoGroupResponse.getDisplayName());
        assertNotNull(discoGroupResponse.getDescription());
        assertNotNull(discoGroupResponse.getTier());

    }

    @Test
    public void testdiscoveryGroupsPutWithResponse() {
        List<DiscoSource> seeds
            = Arrays.asList(new DiscoSource().setKind(DiscoSourceKind.fromString(seedKind)).setName(seedName));
        DiscoGroupData discoGroupData
            = new DiscoGroupData().setName(newGroupName).setDescription(newGroupDescription).setSeeds(seeds);

        DiscoGroup discoGroupResponse = easmClient.createOrReplaceDiscoGroup(newGroupName, discoGroupData);

        assertEquals(newGroupName, discoGroupResponse.getName());
        assertEquals(newGroupName, discoGroupResponse.getDisplayName());
        assertEquals(newGroupDescription, discoGroupResponse.getDescription());
        assertTrue(doSeedsMatch(seeds.get(0), discoGroupResponse.getSeeds().get(0)));
    }

    @Test
    public void testdiscoveryGroupsRunWithResponse() {
        easmClient.runDiscoGroup(knownGroupName);
    }

    @Test
    public void testdiscoveryGroupsListRunsWithResponse() {
        PagedIterable<DiscoRunResult> discoRunPageResponse = easmClient.listRuns(knownGroupName, null, 0);
        assertNotNull(discoRunPageResponse);
    }
}
