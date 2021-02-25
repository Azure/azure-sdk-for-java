// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data;

import com.azure.cosmos.benchmark.linkedin.impl.Constants;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
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


public class InvitationDataGenerator {

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
    private static final String TTL = "ttl";

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
     * Generates the desired batch of records
     *
     * @param invitationRecordCount Number of records we want to create in this invocation
     * @return Map containing desired count of record key to value entries
     */
    public Map<Key, ObjectNode> generate(int invitationRecordCount) {

        // Generate the intended number of records
        final Map<Key, ObjectNode> records = new HashMap<>();
        for (int index = 0; index < invitationRecordCount;) {
            final String inviter = selectUser();
            final String invitee = selectUser();
            final Key key = new Key(inviter, invitee);
            if (inviter.equals(invitee) || records.containsKey(key)) {
                continue;
            }

            final ObjectNode generateRecord = generateRecord(inviter, invitee);
            records.put(key, generateRecord);
            index++;
        }

        return records;
    }

    private String selectUser() {
        final long userId = (long) (RANDOM_GENERATOR.nextFloat() * (_modeledUserCount));
        return String.valueOf(userId);
    }

    private ObjectNode generateRecord(final String inviter, final String invitee) {
        final ObjectNode record = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        record.set(ID, new TextNode(inviter));
        record.set(PARTITIONING_KEY, new TextNode(invitee));
        record.set(ACTIVE, BooleanNode.getTrue());
        record.set(INVITATION_ID, new LongNode(RANDOM_GENERATOR.nextLong()));
        record.set(INVITER, new TextNode(inviter));
        record.set(INVITEE, new TextNode(invitee));
        record.set(INVITATION_STATE, new TextNode(INVITATION_STATE_PENDING));
        final long currentTimeMillis = System.currentTimeMillis();
        record.set(CREATED_AT, new LongNode(currentTimeMillis));
        record.set(VALIDATION_TOKEN, new TextNode(UUID.randomUUID().toString()));
        record.set(CHANGE_TIMESTAMPS, generateChangeTimestamp(currentTimeMillis));
        // TTL Field is an integer on the CosmosDB storage layer, representing seconds since lastModified to expire
        // the document. This value can not be greater than MAX_INT
        //      Ref: https://docs.microsoft.com/en-us/azure/cosmos-db/time-to-live
        record.set(TTL, new IntNode(86400));
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
