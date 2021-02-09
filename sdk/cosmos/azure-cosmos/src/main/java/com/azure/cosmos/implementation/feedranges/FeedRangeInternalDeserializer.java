// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FeedRangeInternalDeserializer extends StdDeserializer<FeedRangeInternal> {
    private static final long serialVersionUID = 1L;

    public FeedRangeInternalDeserializer() {
        this(null);
    }

    public FeedRangeInternalDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public FeedRangeInternal deserialize(final JsonParser parser,
                                         final DeserializationContext context)
        throws IOException {

        final JsonNode rootNode = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        return deserializeFeedRange(rootNode, mapper, parser);
    }

    public static FeedRangeInternal deserializeFeedRange(
        JsonNode rootNode,
        ObjectMapper mapper,
        JsonParser parser) throws JsonMappingException {

        checkNotNull(rootNode, "Argument 'rootNode' must not be null.");
        checkNotNull(mapper, "Argument 'mapper' must not be null.");
        checkNotNull(parser, "Argument 'parser' must not be null.");

        JsonNode rangeNode = rootNode.get(Constants.Properties.RANGE);
        if (rangeNode != null && rangeNode.isObject()) {
            Range<String> range = new Range<>((ObjectNode)rangeNode);
            return new FeedRangeEpkImpl(range);
        }

        JsonNode pkNode = rootNode.get(Constants.Properties.FEED_RANGE_PARTITION_KEY);
        if (pkNode != null && pkNode.isArray()) {
            PartitionKeyInternal pk = mapper.convertValue(pkNode, PartitionKeyInternal.class);
            return new FeedRangePartitionKeyImpl(pk);
        }

        JsonNode pkRangeIdNode =
            rootNode.get(Constants.Properties.FEED_RANGE_PARTITION_KEY_RANGE_ID);
        if (pkRangeIdNode != null && pkRangeIdNode.isTextual()) {
            return new FeedRangePartitionKeyRangeImpl(pkRangeIdNode.asText());
        }

        throw JsonMappingException.from(parser, "Unknown feed range type");
    }
}