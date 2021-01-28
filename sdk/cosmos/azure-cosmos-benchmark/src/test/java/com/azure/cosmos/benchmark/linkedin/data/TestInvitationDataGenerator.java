package com.azure.cosmos.benchmark.linkedin.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInvitationDataGenerator {

    private static final int RECORD_COUNT = 10000;

    @Test(groups="unit")
    public void testGenerate() {
        final InvitationDataGenerator invitationDataGenerator = new InvitationDataGenerator();
        final Map<Key, ObjectNode> results = invitationDataGenerator.generate(RECORD_COUNT);
        assertThat(results.size()).isEqualTo(RECORD_COUNT);
    }
}
