package com.azure.cosmos.benchmark.linkedin.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;


public class TestInvitationDataGenerator {

    private static final int RECORD_COUNT = 10000;

    @Test
    public void testGenerate() {
        final InvitationDataGenerator invitationDataGenerator = new InvitationDataGenerator();
        final Map<Key, ObjectNode> results = invitationDataGenerator.generate(RECORD_COUNT);
        assertEquals(results.size(), RECORD_COUNT);
    }

}
