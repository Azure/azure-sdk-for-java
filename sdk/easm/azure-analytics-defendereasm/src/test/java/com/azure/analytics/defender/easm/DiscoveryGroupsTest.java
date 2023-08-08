package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmDefenderClientTestBase;
import com.azure.analytics.defender.easm.models.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiscoveryGroupsTest extends EasmDefenderClientTestBase {

    String knownGroupName = "University of Kansas";
    String newGroupName = "New disco group Name";
    String newGroupDescription = "This is a description";
    String seedKind = "domain";
    String seedName = "example.org";

    private boolean doSeedsMatch(DiscoSource seedA, DiscoSource seedB){
        return seedA.getKind() == seedB.getKind() && seedA.getName().equals(seedB.getName());
    }

    @Test
    public void testdiscoveryGroupsListWithResponse(){
        DiscoGroupPageResponse discoGroupPageResponse = discoveryGroupsClient.list();
        DiscoGroup DiscoGroup = discoGroupPageResponse.getValue().get(0);
        assertNotNull(DiscoGroup.getName());
        assertNotNull(DiscoGroup.getDisplayName());
        assertNotNull(DiscoGroup.getDescription());
        assertNotNull(DiscoGroup.getTier());
        assertNotNull(DiscoGroup.getId());
    }

    @Test
    public void testdiscoveryGroupsValidateWithResponse(){
        List<DiscoSource> seeds = Arrays.asList(new DiscoSource()
                .setKind(DiscoSourceKind.fromString(seedKind))
                .setName(seedName));
        DiscoGroupData discoGroupRequest = new DiscoGroupData()
                .setName(newGroupName)
                .setDescription(newGroupDescription)
                .setSeeds(seeds)
                .setFrequencyMilliseconds(604800000L)
                .setTier("advanced");
        ValidateResponse validateResponse = discoveryGroupsClient.validate(discoGroupRequest);
        assertNull(validateResponse.getError());
    }

    @Test
    public void testdiscoveryGroupsGetWithResponse(){
        DiscoGroup DiscoGroup = discoveryGroupsClient.get(knownGroupName);
        assertEquals(knownGroupName, DiscoGroup.getId());
        assertEquals(knownGroupName, DiscoGroup.getName());
        assertEquals(knownGroupName, DiscoGroup.getDisplayName());
        assertNotNull(DiscoGroup.getDescription());
        assertNotNull(DiscoGroup.getTier());

    }

    @Test
    public void testdiscoveryGroupsPutWithResponse(){
        List<DiscoSource> seeds = Arrays.asList(new DiscoSource()
                                        .setKind(DiscoSourceKind.fromString(seedKind))
                                        .setName(seedName));
        DiscoGroupData discoGroupRequest = new DiscoGroupData()
                                        .setName(newGroupName)
                                        .setDescription(newGroupDescription)
                                        .setSeeds(seeds);
        DiscoGroup DiscoGroup = discoveryGroupsClient.put(newGroupName, discoGroupRequest);
        assertEquals(newGroupName, DiscoGroup.getName());
        assertEquals(newGroupName, DiscoGroup.getDisplayName());
        assertEquals(newGroupDescription, DiscoGroup.getDescription());
        assertTrue(doSeedsMatch(seeds.get(0), DiscoGroup.getSeeds().get(0)));
    }

    @Test
    public void testdiscoveryGroupsRunWithResponse(){
        discoveryGroupsClient.run(knownGroupName);
    }

    @Test
    public void testdiscoveryGroupsListRunsWithResponse(){
        DiscoRunPageResponse discoRunPageResponse = discoveryGroupsClient.listRuns(knownGroupName);
        DiscoRunResponse discoRunResponse = discoRunPageResponse.getValue().get(0);
        assertNotNull(discoRunResponse.getState());
        assertNotNull(discoRunResponse.getTier());
    }
}
