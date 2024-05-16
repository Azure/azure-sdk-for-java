package com.azure.cosmos.benchmark.linkedin.data;

import com.azure.cosmos.benchmark.linkedin.data.entity.InvitationDataGenerator;
import com.azure.cosmos.benchmark.linkedin.data.entity.InvitationsKeyGenerator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InvitationDataGeneratorTest {

    private static final int RECORD_COUNT = 10000;

    @Test
    public void generate() {
        final InvitationDataGenerator invitationDataGenerator =
            new InvitationDataGenerator(new InvitationsKeyGenerator(RECORD_COUNT));
        final Map<Key, ObjectNode> results = invitationDataGenerator.generate(RECORD_COUNT);
        assertThat(results.size()).isGreaterThan((int) (RECORD_COUNT * 0.90));
    }
}
