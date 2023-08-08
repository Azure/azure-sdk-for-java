// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeedRangeContinuationDeserializer extends StdDeserializer<FeedRangeContinuation> {

    private static final long serialVersionUID = 1L;

    public FeedRangeContinuationDeserializer() {
        this(null);
    }

    public FeedRangeContinuationDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public FeedRangeContinuation deserialize(
        final JsonParser parser,
        final DeserializationContext context) throws IOException {

        final JsonNode rootNode = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        return deserializeFeedRangeContinuation(rootNode, mapper, parser);
    }

    public static FeedRangeContinuation deserializeFeedRangeContinuation(
        JsonNode rootNode,
        ObjectMapper mapper,
        JsonParser parser) throws JsonMappingException {

        JsonNode versionNode =
            rootNode.get(Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_VERSION);
        if (versionNode == null || !versionNode.isInt()) {
            throw JsonMappingException.from(
                parser,
                String.format(
                    "Required property '%s' missing or does not have a valid value.",
                    Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_VERSION));
        }

        JsonNode ridNode =
            rootNode.get(Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_RESOURCE_ID);
        if (ridNode == null || !ridNode.isTextual()) {
            throw JsonMappingException.from(
                parser,
                String.format(
                    "Required property '%s' missing or does not have a valid value.",
                    Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_RESOURCE_ID));
        }
        String containerRid = ridNode.textValue();

        JsonNode continuationNode =
            rootNode.get(Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_CONTINUATION);
        if (continuationNode == null ||
            !continuationNode.isArray() ||
            continuationNode.size() == 0) {
            throw JsonMappingException.from(
                parser,
                String.format(
                    "Required property '%s' missing or does not have a valid value.",
                    Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_CONTINUATION));
        }

        List<CompositeContinuationToken> tokens = new ArrayList<>(continuationNode.size());
        for (int i = 0; i < continuationNode.size(); i++) {
            JsonNode tokenNode = continuationNode.get(i);
            if (tokenNode == null || !tokenNode.isObject()) {
                throw JsonMappingException.from(
                    parser,
                    String.format(
                        "Required property '%s' at index '%d' missing or does not have a valid " +
                            "value.",
                        Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_CONTINUATION,
                        i));
            }

            tokens.add(new CompositeContinuationToken((ObjectNode)tokenNode));
        }

        FeedRangeInternal feedRange = FeedRangeInternalDeserializer
            .deserializeFeedRange(rootNode, mapper, parser);

        return FeedRangeCompositeContinuationImpl
            .createFromDeserializedTokens(
                containerRid,
                feedRange,
                tokens);
    }
}