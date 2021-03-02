// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data;

import com.azure.cosmos.benchmark.linkedin.impl.Constants;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.azure.cosmos.benchmark.linkedin.impl.Constants.PARTITION_KEY;


public class InvitationDataGenerator implements Generator {

    private static final String ID = Constants.ID;
    private static final String PARTITIONING_KEY = PARTITION_KEY;
    private static final String ACTIVE = "active";
    private static final String INVITATION_ID = "invitationId";
    private static final String INVITER = "inviter";
    private static final String INVITEE = "invitee";
    private static final String INVITATION_STATE = "invitationState";
    private static final String INVITATION_STATE_PENDING = "PENDING";
    private static final String CREATED_AT = "createdAt";
    private static final String VALIDATION_TOKEN = "validationToken";
    private static final String CHANGE_TIMESTAMPS = "changeTimeStamps";
    private static final String CREATED = "created";
    private static final String LAST_MODIFIED = "lastModified";
    private static final String DELETED = "deleted";

    private static final JsonNodeFactory JSON_NODE_FACTORY_INSTANCE = JsonNodeFactory.withExactBigDecimals(true);
    private static final Random RANDOM_GENERATOR = new Random();
    private static final double MEMBER_TO_INVITATION_RATIO = 0.36;

    private final long _modeledUserCount;

    public InvitationDataGenerator(int documentCount) {
        Preconditions.checkArgument(documentCount > 0,
            "The numbers of documents to generate must be > 0");
        _modeledUserCount = (long) (documentCount * MEMBER_TO_INVITATION_RATIO);
    }

    /**
     * Generates the desired batch of records for the Invitation entity
     *
     * @param recordCount Number of records we want to create in this invocation
     * @return Map containing desired count of record key to value entries
     */
    @Override
    public Map<Key, ObjectNode> generate(int recordCount) {

        // Generate the intended number of records
        final Map<Key, ObjectNode> records = new HashMap<>();
        for (int index = 0; index < recordCount;) {
            final long inviter = selectUser();
            final long invitee = selectUser();
            final Key key = new Key(inviter, invitee);
            if (inviter == invitee || records.containsKey(key)) {
                continue;
            }

            final ObjectNode generateRecord = generateRecord(inviter, invitee);
            records.put(key, generateRecord);
            index++;
        }

        return records;
    }

    private long selectUser() {
        return (long) (RANDOM_GENERATOR.nextFloat() * (_modeledUserCount));
    }

    private ObjectNode generateRecord(final long inviter, final long invitee) {
        final ObjectNode record = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        final String inviterValue = String.valueOf(inviter);
        final String inviteeValue = String.valueOf(invitee);
        record.set(ID, new TextNode(inviterValue));
        record.set(PARTITIONING_KEY, new TextNode(inviteeValue));
        record.set(ACTIVE, BooleanNode.getTrue());
        record.set(INVITATION_ID, new LongNode(RANDOM_GENERATOR.nextLong()));
        record.set(INVITER, new TextNode(inviterValue));
        record.set(INVITEE, new TextNode(inviteeValue));
        record.set(INVITATION_STATE, new TextNode(INVITATION_STATE_PENDING));
        final long currentTimeMillis = System.currentTimeMillis();
        record.set(CREATED_AT, new LongNode(currentTimeMillis));
        record.set(VALIDATION_TOKEN, new TextNode(UUID.randomUUID().toString()));
        record.set(CHANGE_TIMESTAMPS, generateChangeTimestamp(currentTimeMillis));
        return record;
    }

    private ObjectNode generateChangeTimestamp(long currentTimeMillis) {
        final ObjectNode changeTimestamp = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        changeTimestamp.set(CREATED, new LongNode(currentTimeMillis));
        changeTimestamp.set(LAST_MODIFIED, new LongNode(currentTimeMillis));
        changeTimestamp.set(DELETED, NullNode.getInstance());
        return changeTimestamp;
    }
}
