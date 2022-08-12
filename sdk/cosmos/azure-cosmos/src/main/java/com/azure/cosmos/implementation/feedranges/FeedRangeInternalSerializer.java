// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.Constants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class FeedRangeInternalSerializer extends StdSerializer<FeedRangeInternal> {
    private static final long serialVersionUID = 1L;

    protected FeedRangeInternalSerializer() { this(null); }

    protected FeedRangeInternalSerializer(Class<FeedRangeInternal> t) {
        super(t);
    }

    @Override
    public void serialize(FeedRangeInternal feedRange, JsonGenerator writer, SerializerProvider serializerProvider) {

        try {
            writer.writeStartObject();

            if (feedRange instanceof FeedRangeEpkImpl) {
                writer.writeObjectField(
                    Constants.Properties.RANGE,
                    ((FeedRangeEpkImpl) feedRange).getRange());

            } else if (feedRange instanceof FeedRangePartitionKeyImpl) {
                writer.writeObjectField(
                    Constants.Properties.FEED_RANGE_PARTITION_KEY,
                    ((FeedRangePartitionKeyImpl) feedRange).getPartitionKeyInternal());

            } else if (feedRange instanceof FeedRangePartitionKeyRangeImpl) {
                writer.writeStringField(
                    Constants.Properties.FEED_RANGE_PARTITION_KEY_RANGE_ID,
                    ((FeedRangePartitionKeyRangeImpl) feedRange).getPartitionKeyRangeId());

            } else {
                throw new IllegalStateException("FeedRange type " + feedRange.getClass() + " is not supported");
            }

            writer.writeEndObject();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
