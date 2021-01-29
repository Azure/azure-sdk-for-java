// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.benchmark.linkedin.data.InvitationDataGenerator;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;


public class DataGenerator {

    private DataGenerator() {

    }

    /**
     * Generate N records modeling the Invitation Data stored in CosmosDB
     *
     * @param recordCount Number of records to generate
     * @return Map of Key to JsonNode representing each record
     */
    public static Map<Key, ObjectNode> createInvitationRecords(int recordCount) {
        final InvitationDataGenerator invitationDataGenerator = new InvitationDataGenerator();
        return invitationDataGenerator.generate(recordCount);
    }
}
